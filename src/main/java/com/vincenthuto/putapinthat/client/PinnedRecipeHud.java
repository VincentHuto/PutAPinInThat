package com.vincenthuto.putapinthat.client;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.ArrayList;
import java.util.List;

public final class PinnedRecipeHud {
    private static final int HEADER_HEIGHT = 20;
    private static final int HEADER_TEXT_Y = 6;
    private static final int ICON_SIZE = 16;
    private static final int ICON_MARGIN = 2;
    private static final int HEADER_TEXT_GAP = 4;
    private static final int CLOSE_BUTTON_MARGIN = 2;
    private static final int ABOVE_CHAT_Z = 300;
    private static final int BELOW_CHAT_Z = -300;

    private PinnedRecipeHud() {
    }

    public static void renderBelowChat(
            ForgeGui forgeGui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight) {
        if (!ClientConfig.pinsRenderAboveChat()) {
            render(graphics, screenWidth, screenHeight, BELOW_CHAT_Z);
        }
    }

    public static void renderAboveChat(
            ForgeGui forgeGui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight) {
        if (ClientConfig.pinsRenderAboveChat()) {
            render(graphics, screenWidth, screenHeight, ABOVE_CHAT_Z);
        }
    }

    private static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int zOffset) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        List<Card> cards = layoutCards(screenWidth, screenHeight);
        if (cards.isEmpty()) {
            return;
        }

        double mouseX = scaledMouseX(minecraft);
        double mouseY = scaledMouseY(minecraft);
        boolean interactive = minecraft.screen != null && ClientEvents.canInteractWithPins(minecraft.screen);

        graphics.flush();
        for (Card card : cards) {
            var layout = card.pin().layout();
            var border = layout.getRectWithBorder();
            var recipe = layout.getRect();
            layout.setPosition(recipe.getX() - border.getX(), recipe.getY() - border.getY());

            graphics.pose().pushPose();
            graphics.pose().translate(card.x(), card.y(), zOffset);
            graphics.pose().scale((float) card.scale(), (float) card.scale(), 1.0F);
            drawHeader(graphics, minecraft.font, layout.getRecipeCategory(), border.getWidth(),
                    interactive && card.closeButtonContains(mouseX, mouseY));
            graphics.pose().translate(0, HEADER_HEIGHT, 0);
            layout.drawRecipe(graphics, -10_000, -10_000);
            graphics.pose().popPose();
        }
        graphics.flush();
    }

    public static boolean removePinAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        for (Card card : layoutCards(screenWidth, screenHeight)) {
            if (card.closeButtonContains(mouseX, mouseY)) {
                return PinnedRecipeManager.getInstance().remove(card.pin().key());
            }
        }
        return false;
    }

    private static List<Card> layoutCards(int screenWidth, int screenHeight) {
        List<PinnedRecipeManager.ResolvedPin> pins = PinnedRecipeManager.getInstance().getResolvedPins();
        List<HudStackLayout.Size> sizes = pins.stream()
                .map(PinnedRecipeManager.ResolvedPin::layout)
                .map(layout -> layout.getRectWithBorder())
                .map(rect -> new HudStackLayout.Size(rect.getWidth(), rect.getHeight() + HEADER_HEIGHT))
                .toList();
        HudStackLayout.Result stack = HudStackLayout.calculate(screenWidth, screenHeight, sizes);
        if (stack.cardOffsetsY().isEmpty()) {
            return List.of();
        }

        List<Card> cards = new ArrayList<>(pins.size());
        for (int i = 0; i < pins.size(); i++) {
            var pin = pins.get(i);
            int width = pin.layout().getRectWithBorder().getWidth();
            cards.add(new Card(pin, stack.x(), stack.y() + stack.cardOffsetsY().get(i), stack.scale(), width));
        }
        return List.copyOf(cards);
    }

    private static void drawHeader(
            GuiGraphics graphics,
            Font font,
            IRecipeCategory<?> category,
            int width,
            boolean closeButtonHovered) {
        graphics.fill(0, 0, width, HEADER_HEIGHT, 0xFF000000);
        graphics.fill(1, 1, width - 1, HEADER_HEIGHT - 1, 0xFFD0D0D0);
        graphics.fill(2, 2, width - 2, HEADER_HEIGHT - 2, 0xFFA0A0A0);

        int badgeX = ICON_MARGIN;
        graphics.fill(1, 1, HEADER_HEIGHT - 1, HEADER_HEIGHT - 1, 0xFF4A4A4A);
        graphics.fill(2, 2, HEADER_HEIGHT - 2, HEADER_HEIGHT - 2, 0xFFB8B8B8);
        drawIcon(graphics, category.getIcon(), badgeX, ICON_MARGIN);

        int textX = badgeX + ICON_SIZE + HEADER_TEXT_GAP;
        int closeButtonX = width - CLOSE_BUTTON_MARGIN - ClosePinButtonTexture.SIZE;
        int maxTextWidth = Math.max(0, closeButtonX - textX - HEADER_TEXT_GAP);
        String title = fitTitle(font, category.getTitle().getString(), maxTextWidth);
        graphics.drawString(font, title, textX, HEADER_TEXT_Y, 0xFFFFFFFF, true);
        ClosePinButtonTexture.draw(graphics, closeButtonX, CLOSE_BUTTON_MARGIN, closeButtonHovered);
    }

    private static void drawIcon(GuiGraphics graphics, IDrawable icon, int x, int y) {
        double scale = Math.min(1.0,
                Math.min((double) ICON_SIZE / icon.getWidth(), (double) ICON_SIZE / icon.getHeight()));
        double centeredX = x + (ICON_SIZE - icon.getWidth() * scale) / 2.0;
        double centeredY = y + (ICON_SIZE - icon.getHeight() * scale) / 2.0;

        graphics.pose().pushPose();
        graphics.pose().translate(centeredX, centeredY, 0);
        graphics.pose().scale((float) scale, (float) scale, 1.0F);
        icon.draw(graphics, 0, 0);
        graphics.pose().popPose();
    }

    private static String fitTitle(Font font, String title, int maxWidth) {
        if (maxWidth <= 0 || title.isEmpty()) {
            return "";
        }
        if (font.width(title) <= maxWidth) {
            return title;
        }

        String ellipsis = "...";
        int availableWidth = maxWidth - font.width(ellipsis);
        if (availableWidth <= 0) {
            return "";
        }
        return font.plainSubstrByWidth(title, availableWidth) + ellipsis;
    }

    private static double scaledMouseX(Minecraft minecraft) {
        return minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
    }

    private static double scaledMouseY(Minecraft minecraft) {
        return minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
    }

    private record Card(
            PinnedRecipeManager.ResolvedPin pin,
            double x,
            double y,
            double scale,
            int width) {

        private boolean closeButtonContains(double mouseX, double mouseY) {
            double buttonX = x + (width - CLOSE_BUTTON_MARGIN - ClosePinButtonTexture.SIZE) * scale;
            double buttonY = y + CLOSE_BUTTON_MARGIN * scale;
            double buttonSize = ClosePinButtonTexture.SIZE * scale;
            return mouseX >= buttonX && mouseX < buttonX + buttonSize
                    && mouseY >= buttonY && mouseY < buttonY + buttonSize;
        }
    }
}

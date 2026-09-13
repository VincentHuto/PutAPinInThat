package com.vincenthuto.putapinthat.client;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.List;

public final class PinnedRecipeHud {
    private PinnedRecipeHud() {
    }

    public static void render(ForgeGui forgeGui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        List<IRecipeLayoutDrawable<?>> layouts = PinnedRecipeManager.getInstance().getResolvedLayouts();
        List<HudStackLayout.Size> sizes = layouts.stream()
                .map(IRecipeLayoutDrawable::getRectWithBorder)
                .map(rect -> new HudStackLayout.Size(rect.getWidth(), rect.getHeight()))
                .toList();
        HudStackLayout.Result stack = HudStackLayout.calculate(screenWidth, screenHeight, sizes);
        if (stack.cardOffsetsY().isEmpty()) {
            return;
        }

        for (int i = 0; i < layouts.size(); i++) {
            IRecipeLayoutDrawable<?> layout = layouts.get(i);
            double cardY = stack.cardOffsetsY().get(i);
            var border = layout.getRectWithBorder();
            var recipe = layout.getRect();
            layout.setPosition(recipe.getX() - border.getX(), recipe.getY() - border.getY());

            graphics.pose().pushPose();
            graphics.pose().translate(stack.x(), stack.y() + cardY, 0);
            graphics.pose().scale((float) stack.scale(), (float) stack.scale(), 1.0F);
            layout.drawRecipe(graphics, -10_000, -10_000);
            graphics.pose().popPose();
        }
    }
}

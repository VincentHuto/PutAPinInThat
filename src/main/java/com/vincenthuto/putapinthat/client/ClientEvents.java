package com.vincenthuto.putapinthat.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.putapinthat.jei.ClientPinFeedback;
import com.vincenthuto.putapinthat.mixin.BookmarkOverlayAccessor;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    public static final KeyMapping PIN_RECIPE_KEY = new KeyMapping(
            "key.putapinthat.pin_recipe",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.putapinthat");

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!PIN_RECIPE_KEY.matches(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        double mouseX = scaledMouseX(minecraft);
        double mouseY = scaledMouseY(minecraft);
        PinnedRecipeManager manager = PinnedRecipeManager.getInstance();

        if (event.getScreen() instanceof RecipesGui recipesGui) {
            var layout = recipesGui.getRecipeLayoutUnderMouse(mouseX, mouseY);
            if (layout.isPresent()) {
                ClientPinFeedback.show(manager.toggle(layout.get().getRecipeLayout()));
                event.setCanceled(true);
                return;
            }
        }

        var runtime = manager.getRuntime();
        if (runtime != null && runtime.getBookmarkOverlay() instanceof BookmarkOverlayAccessor accessor) {
            var bookmark = accessor.putapinthat$getContents().getSlots()
                    .filter(slot -> slot.isMouseOver(mouseX, mouseY))
                    .flatMap(slot -> slot.getOptionalElement().stream())
                    .flatMap(element -> element.getBookmark().stream())
                    .filter(RecipeBookmark.class::isInstance)
                    .map(RecipeBookmark.class::cast)
                    .findFirst();
            if (bookmark.isPresent()) {
                ClientPinFeedback.show(manager.toggle(bookmark.get()));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT || !canShowClearButton(event.getScreen())) {
            return;
        }
        ClearButtonBounds bounds = ClearButtonBounds.forScreenHeight(event.getScreen().height);
        if (bounds.contains(event.getMouseX(), event.getMouseY())) {
            PinnedRecipeManager.getInstance().clear();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (!canShowClearButton(event.getScreen())) {
            return;
        }
        ClearButtonBounds bounds = ClearButtonBounds.forScreenHeight(event.getScreen().height);
        boolean hovered = bounds.contains(event.getMouseX(), event.getMouseY());
        drawClearButton(event.getGuiGraphics(), bounds, hovered);
        if (hovered) {
            event.getGuiGraphics().renderTooltip(Minecraft.getInstance().font,
                    Component.translatable("tooltip.putapinthat.clear"), event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PinnedRecipeManager.getInstance().tick();
        }
    }

    private static boolean canShowClearButton(Screen screen) {
        PinnedRecipeManager manager = PinnedRecipeManager.getInstance();
        var runtime = manager.getRuntime();
        return runtime != null && !manager.isEmpty()
                && runtime.getBookmarkOverlay() instanceof BookmarkOverlay
                && runtime.getScreenHelper().getGuiProperties(screen).isPresent();
    }

    private static void drawClearButton(GuiGraphics graphics, ClearButtonBounds bounds, boolean hovered) {
        ClearPinsButtonTexture.draw(graphics, bounds.x(), bounds.y(), hovered);
    }

    private static double scaledMouseX(Minecraft minecraft) {
        return minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
    }

    private static double scaledMouseY(Minecraft minecraft) {
        return minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
    }
}

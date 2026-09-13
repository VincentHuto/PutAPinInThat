package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class ClearPinsButtonTexture {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(
            PutAPinInThat.MOD_ID, "textures/gui/clear_pins.png");
    private static final ResourceLocation ENABLED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "jei", "textures/jei/atlas/gui/button_enabled.png");
    private static final ResourceLocation HOVERED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "jei", "textures/jei/atlas/gui/button_highlight.png");

    private static IDrawable icon;
    private static IDrawable enabledBackground;
    private static IDrawable hoveredBackground;

    private ClearPinsButtonTexture() {
    }

    public static void initialize(IGuiHelper guiHelper) {
        icon = guiHelper.drawableBuilder(ICON, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        enabledBackground = guiHelper.drawableBuilder(ENABLED_BACKGROUND, 0, 0, 20, 20)
                .setTextureSize(20, 20)
                .build();
        hoveredBackground = guiHelper.drawableBuilder(HOVERED_BACKGROUND, 0, 0, 20, 20)
                .setTextureSize(20, 20)
                .build();
    }

    public static void draw(GuiGraphics graphics, int x, int y, boolean hovered) {
        ensureInitialized();
        (hovered ? hoveredBackground : enabledBackground).draw(graphics, x, y);
        icon.draw(graphics, x + 2, y + 2);
    }

    private static void ensureInitialized() {
        if (icon == null || enabledBackground == null || hoveredBackground == null) {
            throw new IllegalStateException("Clear-pins button textures requested before JEI registration");
        }
    }
}

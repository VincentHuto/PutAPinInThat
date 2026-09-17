package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class ClosePinButtonTexture {
    public static final int SIZE = 16;

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            PutAPinInThat.MOD_ID, "textures/gui/close_pin.png");
    private static IDrawable normal;
    private static IDrawable hovered;

    private ClosePinButtonTexture() {
    }

    public static void initialize(IGuiHelper guiHelper) {
        normal = guiHelper.drawableBuilder(TEXTURE, 0, 0, SIZE, SIZE)
                .setTextureSize(SIZE * 2, SIZE)
                .build();
        hovered = guiHelper.drawableBuilder(TEXTURE, SIZE, 0, SIZE, SIZE)
                .setTextureSize(SIZE * 2, SIZE)
                .build();
    }

    public static void draw(GuiGraphics graphics, int x, int y, boolean isHovered) {
        ensureInitialized();
        (isHovered ? hovered : normal).draw(graphics, x, y);
    }

    private static void ensureInitialized() {
        if (normal == null || hovered == null) {
            throw new IllegalStateException("Close-pin button texture requested before JEI registration");
        }
    }
}

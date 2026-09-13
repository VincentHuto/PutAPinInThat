package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class PinTexture {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            PutAPinInThat.MOD_ID, "textures/gui/pin.png");
    private static IDrawable drawable;

    private PinTexture() {
    }

    public static void initialize(IGuiHelper guiHelper) {
        drawable = guiHelper.drawableBuilder(TEXTURE, 0, 0, 9, 10)
                .setTextureSize(9, 10)
                .build();
    }

    public static IDrawable drawable() {
        if (drawable == null) {
            throw new IllegalStateException("Pin texture requested before JEI registration");
        }
        return drawable;
    }

    public static void draw(GuiGraphics graphics, int x, int y) {
        drawable().draw(graphics, x, y);
    }
}

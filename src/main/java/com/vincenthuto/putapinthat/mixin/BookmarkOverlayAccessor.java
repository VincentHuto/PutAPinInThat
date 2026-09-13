package com.vincenthuto.putapinthat.mixin;

import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BookmarkOverlay.class, remap = false)
public interface BookmarkOverlayAccessor {
    @Accessor("contents")
    IngredientGridWithNavigation putapinthat$getContents();
}

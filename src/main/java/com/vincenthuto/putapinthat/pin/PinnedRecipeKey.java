package com.vincenthuto.putapinthat.pin;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PinnedRecipeKey(ResourceLocation recipeTypeId, ResourceLocation recipeId) {
    public PinnedRecipeKey {
        Objects.requireNonNull(recipeTypeId, "recipeTypeId");
        Objects.requireNonNull(recipeId, "recipeId");
    }
}

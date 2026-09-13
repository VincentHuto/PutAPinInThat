package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import com.vincenthuto.putapinthat.pin.PinnedRecipeKey;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public final class RecipeIdentity {
    private static final String SYNTHETIC_PATH = "synthetic/";

    private RecipeIdentity() {
    }

    public static Optional<PinnedRecipeKey> create(
            IRecipeLayoutDrawable<?> layout,
            IIngredientManager ingredientManager) {
        var category = layout.getRecipeCategory();
        ResourceLocation typeId = category.getRecipeType().getUid();
        ResourceLocation registeredId = getRegisteredId(layout);
        if (registeredId != null) {
            return Optional.of(new PinnedRecipeKey(typeId, registeredId));
        }

        MessageDigest digest = sha256();
        update(digest, typeId.toString());
        update(digest, layout.getRecipe().getClass().getName());
        if (layout.getRecipe() instanceof IJeiFuelingRecipe fuelingRecipe) {
            update(digest, fuelingRecipe.getBurnTime());
        }
        int ingredientCount = 0;

        var slots = layout.getRecipeSlotsView().getSlotViews();
        update(digest, slots.size());
        for (var slot : slots) {
            update(digest, slot.getRole().name());
            update(digest, slot.getSlotName().orElse(""));
            List<String> ingredientUids = slot.getAllIngredientsList().stream()
                    .map(ingredient -> getIngredientUid(ingredientManager, ingredient))
                    .sorted()
                    .toList();
            ingredientCount += ingredientUids.size();
            update(digest, ingredientUids.size());
            ingredientUids.forEach(uid -> update(digest, uid));
        }

        if (ingredientCount == 0) {
            return Optional.empty();
        }
        String hash = java.util.HexFormat.of().formatHex(digest.digest());
        ResourceLocation syntheticId = ResourceLocation.fromNamespaceAndPath(
                PutAPinInThat.MOD_ID, SYNTHETIC_PATH + hash);
        return Optional.of(new PinnedRecipeKey(typeId, syntheticId));
    }

    public static boolean isSynthetic(PinnedRecipeKey key) {
        return PutAPinInThat.MOD_ID.equals(key.recipeId().getNamespace())
                && key.recipeId().getPath().startsWith(SYNTHETIC_PATH);
    }

    private static <T> ResourceLocation getRegisteredId(IRecipeLayoutDrawable<T> layout) {
        return layout.getRecipeCategory().getRegistryName(layout.getRecipe());
    }

    private static <T> String getIngredientUid(
            IIngredientManager ingredientManager,
            ITypedIngredient<T> ingredient) {
        String typeUid = ingredient.getType().getUid();
        var helper = ingredientManager.getIngredientHelper(ingredient.getType());
        String valueUid = helper.getUniqueId(ingredient, UidContext.Recipe);
        long amount = helper.getAmount(ingredient.getIngredient());
        return typeUid + '\0' + valueUid + '\0' + amount;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        update(digest, bytes.length);
        digest.update(bytes);
    }

    private static void update(MessageDigest digest, int value) {
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
    }
}

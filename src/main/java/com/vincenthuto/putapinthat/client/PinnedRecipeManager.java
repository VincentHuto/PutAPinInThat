package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import com.vincenthuto.putapinthat.pin.PinToggleResult;
import com.vincenthuto.putapinthat.pin.PinnedRecipeKey;
import com.vincenthuto.putapinthat.pin.PinnedRecipeList;
import com.vincenthuto.putapinthat.pin.PinnedRecipeStore;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class PinnedRecipeManager {
    private static final PinnedRecipeManager INSTANCE = new PinnedRecipeManager();
    private static final int MAX_PINS = 4;

    private final PinnedRecipeList pins = new PinnedRecipeList(MAX_PINS);
    private final PinnedRecipeStore store = new PinnedRecipeStore(savePath());
    private final Map<PinnedRecipeKey, IRecipeLayoutDrawable<?>> layouts = new LinkedHashMap<>();
    private final Map<IRecipeLayoutDrawable<?>, Optional<PinnedRecipeKey>> identities = new WeakHashMap<>();
    private IJeiRuntime runtime;
    private boolean rebuildPending;

    private PinnedRecipeManager() {
    }

    public static PinnedRecipeManager getInstance() {
        return INSTANCE;
    }

    public void load() {
        pins.replaceLoaded(store.load());
        rebuildPending = runtime != null;
    }

    public void setRuntime(IJeiRuntime runtime) {
        this.runtime = runtime;
        layouts.clear();
        rebuildPending = true;
    }

    public void clearRuntime() {
        runtime = null;
        rebuildPending = false;
        layouts.clear();
        identities.clear();
    }

    public PinToggleResult toggle(IRecipeLayoutDrawable<?> layout) {
        Optional<PinnedRecipeKey> key = keyOf(layout);
        if (key.isEmpty()) {
            return PinToggleResult.UNSUPPORTED;
        }
        PinToggleResult result = pins.toggle(key.get());
        if (result == PinToggleResult.ADDED) {
            layouts.keySet().retainAll(pins.entries());
            createLayoutCopy(key.get(), layout);
        } else if (result == PinToggleResult.REMOVED) {
            layouts.remove(key.get());
        }
        if (result == PinToggleResult.ADDED || result == PinToggleResult.REMOVED) {
            save();
        }
        return result;
    }

    public PinToggleResult toggle(RecipeBookmark<?, ?> bookmark) {
        PinnedRecipeKey key = new PinnedRecipeKey(
                bookmark.getRecipeCategory().getRecipeType().getUid(),
                bookmark.getRecipeUid());
        PinToggleResult result = pins.toggle(key);
        if (result == PinToggleResult.ADDED) {
            layouts.keySet().retainAll(pins.entries());
            createBookmarkLayout(key, bookmark);
        } else if (result == PinToggleResult.REMOVED) {
            layouts.remove(key);
        }
        if (result == PinToggleResult.ADDED || result == PinToggleResult.REMOVED) {
            save();
        }
        return result;
    }

    public boolean isPinned(IRecipeLayoutDrawable<?> layout) {
        return keyOf(layout).filter(pins::contains).isPresent();
    }

    public boolean isEmpty() {
        return pins.isEmpty();
    }

    public void clear() {
        if (pins.isEmpty()) {
            return;
        }
        pins.clear();
        layouts.clear();
        save();
    }

    public boolean remove(PinnedRecipeKey key) {
        if (!pins.remove(key)) {
            return false;
        }
        layouts.remove(key);
        save();
        return true;
    }

    public List<ResolvedPin> getResolvedPins() {
        List<ResolvedPin> ordered = new ArrayList<>();
        for (PinnedRecipeKey key : pins.entries()) {
            IRecipeLayoutDrawable<?> layout = layouts.get(key);
            if (layout != null) {
                ordered.add(new ResolvedPin(key, layout));
            }
        }
        return List.copyOf(ordered);
    }

    public void tick() {
        if (rebuildPending && runtime != null) {
            rebuildPending = false;
            rebuildLayouts();
        }
        layouts.values().forEach(IRecipeLayoutDrawable::tick);
    }

    private Optional<PinnedRecipeKey> keyOf(IRecipeLayoutDrawable<?> layout) {
        if (runtime == null) {
            return Optional.empty();
        }
        return identities.computeIfAbsent(layout,
                candidate -> RecipeIdentity.create(candidate, runtime.getIngredientManager()));
    }

    private void rebuildLayouts() {
        layouts.clear();
        if (runtime == null) {
            return;
        }
        for (PinnedRecipeKey key : pins.entries()) {
            runtime.getRecipeManager().getRecipeType(key.recipeTypeId()).ifPresent(type -> resolve(key, type));
        }
    }

    private <T> void resolve(PinnedRecipeKey key, RecipeType<T> type) {
        IRecipeCategory<T> category = runtime.getRecipeManager().getRecipeCategory(type);
        if (!RecipeIdentity.isSynthetic(key)) {
            runtime.getRecipeManager().createRecipeLookup(type).includeHidden().get()
                    .filter(recipe -> key.recipeId().equals(category.getRegistryName(recipe)))
                    .findFirst()
                    .flatMap(recipe -> createLayout(category, recipe))
                    .ifPresent(layout -> layouts.put(key, layout));
            return;
        }
        runtime.getRecipeManager().createRecipeLookup(type).includeHidden().get()
                .map(recipe -> createLayout(category, recipe))
                .flatMap(Optional::stream)
                .filter(layout -> keyOf(layout).filter(key::equals).isPresent())
                .findFirst()
                .ifPresent(layout -> layouts.put(key, layout));
    }

    private <T> Optional<IRecipeLayoutDrawable<T>> createLayout(IRecipeCategory<T> category, T recipe) {
        if (runtime == null) {
            return Optional.empty();
        }
        return runtime.getRecipeManager().createRecipeLayoutDrawable(
                category, recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
    }

    private <T> void createLayoutCopy(PinnedRecipeKey key, IRecipeLayoutDrawable<T> source) {
        createLayout(source.getRecipeCategory(), source.getRecipe()).ifPresent(layout -> layouts.put(key, layout));
    }

    private <T> void createBookmarkLayout(PinnedRecipeKey key, RecipeBookmark<T, ?> bookmark) {
        createLayout(bookmark.getRecipeCategory(), bookmark.getRecipe()).ifPresent(layout -> layouts.put(key, layout));
    }

    private void save() {
        try {
            store.save(pins.entries());
        } catch (IOException e) {
            PutAPinInThat.LOGGER.error("Unable to save pinned JEI recipes", e);
        }
    }

    public IJeiRuntime getRuntime() {
        return runtime;
    }

    private static Path savePath() {
        return FMLPaths.CONFIGDIR.get().resolve(PutAPinInThat.MOD_ID).resolve("pins.json");
    }

    public record ResolvedPin(PinnedRecipeKey key, IRecipeLayoutDrawable<?> layout) {
    }
}

package com.vincenthuto.putapinthat.pin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class PinnedRecipeList {
    private final int capacity;
    private final List<PinnedRecipeKey> entries = new ArrayList<>();

    public PinnedRecipeList(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public PinToggleResult toggle(PinnedRecipeKey key) {
        if (entries.remove(key)) {
            return PinToggleResult.REMOVED;
        }
        if (entries.size() >= capacity) {
            entries.remove(0);
        }
        entries.add(key);
        return PinToggleResult.ADDED;
    }

    public boolean contains(PinnedRecipeKey key) {
        return entries.contains(key);
    }

    public boolean remove(PinnedRecipeKey key) {
        return entries.remove(key);
    }

    public List<PinnedRecipeKey> entries() {
        return List.copyOf(entries);
    }

    public void replaceLoaded(List<PinnedRecipeKey> loaded) {
        entries.clear();
        new LinkedHashSet<>(loaded).stream()
                .limit(capacity)
                .forEach(entries::add);
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}

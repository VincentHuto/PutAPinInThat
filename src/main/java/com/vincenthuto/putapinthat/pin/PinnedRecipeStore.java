package com.vincenthuto.putapinthat.pin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class PinnedRecipeStore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FORMAT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    public PinnedRecipeStore(Path file) {
        this.file = file;
    }

    public List<PinnedRecipeKey> load() {
        if (!Files.isRegularFile(file)) {
            return List.of();
        }
        try {
            StoredPins stored = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), StoredPins.class);
            if (stored == null || stored.version != FORMAT_VERSION || stored.pins == null) {
                return List.of();
            }
            List<PinnedRecipeKey> result = new ArrayList<>();
            for (StoredPin pin : stored.pins) {
                if (pin == null) {
                    continue;
                }
                ResourceLocation type = pin.type == null ? null : ResourceLocation.tryParse(pin.type);
                ResourceLocation recipe = pin.recipe == null ? null : ResourceLocation.tryParse(pin.recipe);
                if (type != null && recipe != null) {
                    result.add(new PinnedRecipeKey(type, recipe));
                }
            }
            return List.copyOf(result);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Unable to read pinned recipes from {}", file, exception);
            return List.of();
        }
    }

    public void save(List<PinnedRecipeKey> pins) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<StoredPin> rows = pins.stream()
                .map(pin -> new StoredPin(pin.recipeTypeId().toString(), pin.recipeId().toString()))
                .toList();
        String json = GSON.toJson(new StoredPins(FORMAT_VERSION, rows));
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record StoredPins(int version, List<StoredPin> pins) {
    }

    private record StoredPin(String type, String recipe) {
    }
}

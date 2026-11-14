/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class MapPathFiles {
    public static String filename(String label) {
        String name =
                label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "-").replaceAll("^-|-$", "");

        return (name.isEmpty() ? "map-path" : name) + ".json";
    }

    public static JsonObject provider(MapPathImpl path) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        JsonObject features = new JsonObject();
        features.add("locations", new JsonArray());
        features.add("areas", new JsonArray());
        JsonArray paths = new JsonArray();
        paths.add(pathJson(path));
        features.add("paths", paths);
        json.add("features", features);
        json.add("categories", new JsonArray());
        json.add("icons", new JsonArray());

        return json;
    }

    public static JsonObject pathJson(MapPathImpl path) {
        JsonObject json = Managers.Json.GSON.toJsonTree(path).getAsJsonObject();
        removeNulls(json);

        return json;
    }

    public static JsonObject read(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            WynntilsMod.warn("Could not read map path provider '" + file + "'", e);

            return null;
        }
    }

    public static boolean write(Path file, JsonObject json, boolean replace) {
        Path temporary = null;
        try {
            JsonProvider validated = Managers.Json.GSON.fromJson(json, JsonProvider.class);
            if (validated == null) {
                WynntilsMod.warn("Could not write invalid map path provider '" + file + "'");

                return false;
            }

            if (!replace && Files.exists(file)) {
                WynntilsMod.warn("Map path provider already exists: " + file);

                return false;
            }

            Files.createDirectories(file.toAbsolutePath().getParent());
            temporary = Files.createTempFile(file.toAbsolutePath().getParent(), ".map-path-", ".tmp");
            Files.writeString(temporary, Managers.Json.GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
            if (replace) {
                try {
                    Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // Do not overwrite a provider that appeared while the file was being prepared.
                Files.move(temporary, file);
            }

            return true;
        } catch (IOException | RuntimeException e) {
            WynntilsMod.warn("Could not write map path provider '" + file + "'", e);

            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    WynntilsMod.warn("Could not remove temporary map path file '" + temporary + "'", e);
                }
            }
        }
    }

    private static void removeNulls(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            object.entrySet().removeIf(entry -> entry.getValue().isJsonNull());
            object.entrySet().forEach(entry -> removeNulls(entry.getValue()));
        } else if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(MapPathFiles::removeNulls);
        }
    }
}

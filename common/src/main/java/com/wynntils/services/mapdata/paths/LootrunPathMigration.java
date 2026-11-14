/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class LootrunPathMigration {
    public static JsonObject convert(String label, JsonObject legacy) {
        try {
            JsonArray points = legacy.getAsJsonArray("points");
            if (points == null || points.size() < 2) {
                WynntilsMod.warn("Could not migrate lootrun '" + label + "': a route needs at least two points");

                return null;
            }

            JsonObject attributes = new JsonObject();
            attributes.addProperty("label", label);
            attributes.addProperty("priority", 900);
            attributes.addProperty("pathFillColor", "#55FFFF");
            attributes.addProperty("pathBorderColor", "#000000");
            attributes.addProperty("pathBorderWidth", 1);
            attributes.addProperty("pathWidth", 4);
            JsonObject path = new JsonObject();
            path.addProperty(
                    "featureId", "wynntils:path:" + UUID.nameUUIDFromBytes(label.getBytes(StandardCharsets.UTF_8)));
            path.addProperty("categoryId", "wynntils:path");
            path.add("attributes", attributes);
            path.add("path", points.deepCopy());
            path.add("highlights", legacy.has("chests") ? legacy.get("chests").deepCopy() : new JsonArray());
            JsonArray notes = new JsonArray();
            if (legacy.has("notes") && legacy.get("notes").isJsonArray()) {
                for (JsonElement value : legacy.getAsJsonArray("notes")) {
                    JsonObject oldNote = value.getAsJsonObject();
                    JsonObject note = new JsonObject();
                    note.add(
                            "position",
                            oldNote.has("position")
                                    ? oldNote.get("position").deepCopy()
                                    : oldNote.get("location").deepCopy());
                    note.add("text", normalizeLegacyText(oldNote.get("note")));
                    notes.add(note);
                }
            }

            path.add("notes", notes);
            JsonArray paths = new JsonArray();
            paths.add(path);
            JsonObject features = new JsonObject();
            features.add("locations", new JsonArray());
            features.add("areas", new JsonArray());
            features.add("paths", paths);
            JsonObject provider = new JsonObject();
            provider.addProperty("version", 1);
            provider.addProperty("description", "Migrated lootrun: " + label);
            provider.add("features", features);
            provider.add("categories", new JsonArray());
            provider.add("icons", new JsonArray());

            return provider;
        } catch (RuntimeException e) {
            WynntilsMod.warn("Could not convert legacy lootrun '" + label + "'", e);

            return null;
        }
    }

    public static List<Path> migrate(Path legacyDirectory, Path providersDirectory) {
        List<Path> created = new ArrayList<>();
        if (!Files.isDirectory(legacyDirectory)) return created;

        try (Stream<Path> files = Files.list(legacyDirectory)) {
            for (Path source : files.filter(
                            p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .sorted()
                    .toList()) {
                try {
                    String label = source.getFileName().toString().replaceFirst("\\.json$", "");
                    String hash = checksum(Files.readAllBytes(source));
                    if (hash == null) continue;

                    String slug = MapPathFiles.filename(label).replaceFirst("\\.json$", "");
                    String id = UUID.nameUUIDFromBytes(
                                    source.getFileName().toString().getBytes(StandardCharsets.UTF_8))
                            .toString()
                            .substring(0, 8);
                    Path destination = providersDirectory.resolve("lootrun-" + slug + "-" + id + ".json");
                    if (Files.exists(destination)) {
                        JsonObject existing = MapPathFiles.read(destination);
                        if (existing == null) continue;

                        if (!existing.has("legacyLootrunChecksum")
                                || !hash.equals(
                                        existing.get("legacyLootrunChecksum").getAsString())) {
                            WynntilsMod.warn(
                                    "Migration destination already exists; leaving source untouched: " + destination);
                            continue;
                        }

                        if (Managers.Json.GSON.fromJson(existing, JsonProvider.class) == null) {
                            WynntilsMod.warn("Invalid migration destination; leaving source untouched: " + destination);
                            continue;
                        }
                    } else {
                        JsonObject legacy = MapPathFiles.read(source);
                        if (legacy == null) continue;

                        JsonObject provider = convert(label, legacy);
                        if (provider == null) continue;

                        provider.addProperty("legacyLootrunChecksum", hash);
                        if (!MapPathFiles.write(destination, provider, false)) continue;

                        created.add(destination);
                    }

                    Path archive = legacyDirectory.resolve("migrated");
                    Files.createDirectories(archive);
                    Files.move(source, archive.resolve(source.getFileName()));
                } catch (Exception e) {
                    WynntilsMod.warn("Could not migrate lootrun '" + source + "'; original file retained", e);
                }
            }
        } catch (IOException e) {
            WynntilsMod.warn("Could not list legacy lootruns for migration", e);
        }

        return created;
    }

    private static JsonElement normalizeLegacyText(JsonElement text) {
        JsonElement normalized = text.deepCopy();
        if (normalized.isJsonArray()) {
            for (int i = 0; i < normalized.getAsJsonArray().size(); i++) {
                normalized
                        .getAsJsonArray()
                        .set(i, normalizeLegacyText(normalized.getAsJsonArray().get(i)));
            }
        } else if (normalized.isJsonObject()) {
            JsonObject object = normalized.getAsJsonObject();
            for (String key : List.of("bold", "italic", "underlined", "strikethrough", "obfuscated")) {
                JsonElement value = object.get(key);
                if (value != null
                        && value.isJsonPrimitive()
                        && value.getAsJsonPrimitive().isString()
                        && (value.getAsString().equalsIgnoreCase("true")
                                || value.getAsString().equalsIgnoreCase("false"))) {
                    object.addProperty(key, Boolean.parseBoolean(value.getAsString()));
                }
            }

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    entry.setValue(normalizeLegacyText(entry.getValue()));
                }
            }
        }

        return normalized;
    }

    private static String checksum(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            WynntilsMod.warn("Could not checksum legacy lootrun", e);

            return null;
        }
    }
}

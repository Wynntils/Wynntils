/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.google.gson.stream.MalformedJsonException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import com.wynntils.services.mapdata.type.MapIcon;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class JsonProvider implements MapDataProvider {
    // Note: The version field is not used, but is kept for future compatibility
    //       If the need arises, it can be used to handle different versions of the json format
    //       This is easily achieved by GSON switching to different deserializers based on the version
    private final int version;
    private final JsonFeatures features;
    private final List<MapCategory> categories;
    private final List<MapIcon> icons;

    public JsonProvider(int version, JsonFeatures features, List<MapCategory> categories, List<MapIcon> icons) {
        this.version = version;
        this.features = features;
        this.categories = categories;
        this.icons = icons;
    }

    /**
     * Load a bundled resource from the mod jar
     *
     * @param id       The id of the resource
     * @param filename The filename of the resource
     * @return The loaded json provider
     */
    public static JsonProvider loadBundledResource(String id, String filename) {
        try (InputStream inputStream = WynntilsMod.getModResourceAsStream(filename);
                Reader targetReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return Managers.Json.GSON.fromJson(targetReader, JsonProvider.class);
        } catch (MalformedJsonException e) {
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        } catch (IOException e) {
            WynntilsMod.warn("Error reading map data for '" + id + "'", e);
        } catch (Throwable e) {
            // This is typically a NPE in GSON parsing
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        }
        return null;
    }

    /**
     * Load a local resource from a file
     *
     * @param id   The id of the resource
     * @param file The file to load
     * @return The loaded json provider
     */
    public static JsonProvider loadLocalFile(String id, File file) {
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return Managers.Json.GSON.fromJson(reader, JsonProvider.class);
        } catch (MalformedJsonException e) {
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        } catch (IOException e) {
            WynntilsMod.warn("Error reading map data for '" + id + "'", e);
        } catch (Throwable e) {
            // This is either a json parse error or a NPE in GSON parsing
            WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
        }
        return null;
    }

    /**
     * Load an online resource from a url
     *
     * @param id               The id of the resource
     * @param url              The url to load
     * @param registerCallback The callback to call with the loaded provider
     */
    public static void loadOnlineResource(String id, String url, BiConsumer<String, JsonProvider> registerCallback) {
        Download dl = Managers.Net.download(URI.create(url), id);
        dl.handleReader(
                reader -> {
                    try {
                        registerCallback.accept(id, Managers.Json.GSON.fromJson(reader, JsonProvider.class));
                    } catch (Throwable e) {
                        // This is either a json parse error or a NPE in GSON parsing
                        WynntilsMod.warn("Error parsing map data for '" + id + "'", e);
                    }
                },
                onError -> {
                    WynntilsMod.warn("Error occurred while downloading map data for '" + id + "'", onError);
                });
    }

    public int getVersion() {
        return version;
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return features != null ? features.stream() : Stream.empty();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return categories != null ? categories.stream() : Stream.empty();
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return icons != null ? icons.stream() : Stream.empty();
    }

    @Override
    public void onChange(Consumer<MapDataProvidedType> callback) {
        // The json does not change, as long as we do not implement a
        // reload of the file, so we do not need to register callbacks.
    }

    @Override
    public void reloadData() {
        // Json providers do not need to reload data, as the whole object is reloaded by the owner service instead
    }
}

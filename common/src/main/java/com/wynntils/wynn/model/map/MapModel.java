/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.wynn.model.map.poi.Label;
import com.wynntils.wynn.model.map.poi.MapLocation;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class MapModel extends Model {
    public static final Gson GSON = new GsonBuilder().create();
    private static List<MapProfile> maps = new ArrayList<>();
    private static List<Label> labels = List.of();
    private static Map<String, List<MapLocation>> services = new HashMap<>();

    public static void init() {
        loadLabels();
        loadServices();
        tryLoadMaps();
    }

    public static void reset() {
        maps.clear();
    }

    public static List<MapProfile> getMaps() {
        return maps;
    }

    public static List<Label> getLabels() {
        return labels;
    }

    public static Map<String, List<MapLocation>> getServices() {
        return services;
    }

    public static void loadLabels() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");

        String url = "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";

        RequestHandler handler = WebManager.getHandler();

        handler.addAndDispatch(new RequestBuilder(url, "maps-places")
                .cacheTo(new File(mapDirectory, "places.json"))
                .useCacheAsBackup()
                .handleJsonObject(json -> {
                    PlacesProfile places = GSON.fromJson(json, PlacesProfile.class);
                    labels = places.labels;
                    return true;
                })
                .build());
    }

    public static void loadServices() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");

        String url = "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";

        RequestHandler handler = WebManager.getHandler();

        handler.addAndDispatch(new RequestBuilder(url, "maps-services")
                .cacheTo(new File(mapDirectory, "services.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<ServiceProfile>>() {}.getType();

                    List<ServiceProfile> serviceList = GSON.fromJson(json, type);
                    for (var service : serviceList) {
                        services.put(service.type, service.locations);
                    }

                    return true;
                })
                .build());
    }

    public static CompletableFuture<Boolean> tryLoadMaps() {
        if (WebManager.getApiUrl("AMainMap") == null) return CompletableFuture.completedFuture(false);

        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");

        String url = WebManager.getApiUrl("AMainMap");

        CompletableFuture<Boolean> result = new CompletableFuture<>();
        RequestHandler handler = WebManager.getHandler();

        handler.addAndDispatch(new RequestBuilder(url, "maps")
                .cacheTo(new File(mapDirectory, "maps.json"))
                .useCacheAsBackup()
                .handleJson(json -> {
                    String fileBase = url.substring(0, url.lastIndexOf("/") + 1);

                    JsonArray mapArray = json.getAsJsonArray();

                    final List<MapProfile> syncList = Collections.synchronizedList(new ArrayList<>());

                    for (JsonElement mapData : mapArray) {
                        JsonObject mapObject = mapData.getAsJsonObject();

                        // Final since used in closure
                        final int x1 = mapObject.get("x1").getAsInt();
                        final int z1 = mapObject.get("z1").getAsInt();
                        final int x2 = mapObject.get("x2").getAsInt();
                        final int z2 = mapObject.get("z2").getAsInt();

                        final String file = mapObject.get("file").getAsString();

                        String md5 = mapObject.get("hash").getAsString();

                        // TODO DownloaderManager? + Overlay
                        handler.addRequest(new RequestBuilder(fileBase + file, file)
                                .cacheTo(new File(mapDirectory, file))
                                .cacheMD5Validator(md5)
                                .useCacheAsBackup()
                                .handle(bytes -> {
                                    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                                        NativeImage nativeImage = NativeImage.read(in);

                                        syncList.add(new MapProfile(file, nativeImage, x1, z1, x2, z2));
                                    } catch (IOException e) {
                                        WynntilsMod.info("IOException occurred while loading map image of " + file);
                                        return false; // don't cache
                                    }

                                    return true;
                                })
                                .build());
                    }

                    // hacky way to know when handler has finished dispatching
                    CompletableFuture.runAsync(handler::dispatch).whenComplete((a, b) -> {
                        if (syncList.size() == mapArray.size()) {
                            result.complete(true);
                            maps = syncList;
                        } else {
                            result.complete(false);
                        }
                    });

                    return true;
                })
                .build());

        return result;
    }

    public static boolean isMapLoaded() {
        return !maps.isEmpty();
    }

    public static class PlacesProfile {
        List<Label> labels;
    }

    public static class ServiceProfile {
        String type;
        List<MapLocation> locations;
    }
}

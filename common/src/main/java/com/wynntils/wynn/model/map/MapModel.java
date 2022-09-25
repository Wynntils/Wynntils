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
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.MapLocation;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MapModel extends Model {
    public static final String PLACES_JSON_URL = "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";
    public static final String SERVICES_JSON_URL = "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";
    public static final Gson GSON = new GsonBuilder().create();
    private static List<MapProfile> maps = new ArrayList<>();
    private static Set<Poi> allPois = new HashSet<>();

    public static void init() {
        loadPlaces();
        loadServices();
        tryLoadMaps();
    }

    public static void reset() {
        maps.clear();
    }

    public static List<MapProfile> getMaps() {
        return maps;
    }

    public static Set<Poi> getAllPois() {
        return allPois;
    }

    public static void loadPlaces() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");
        RequestHandler handler = WebManager.getHandler();
        handler.addAndDispatch(new RequestBuilder(PLACES_JSON_URL, "maps-places")
                .cacheTo(new File(mapDirectory, "places.json"))
                .useCacheAsBackup()
                .handleJsonObject(json -> {
                    PlacesProfile places = GSON.fromJson(json, PlacesProfile.class);
                    for (Label label : places.labels) {
                        allPois.add(new LabelPoi(label));
                    }
                    return true;
                })
                .build());
    }

    public static void loadServices() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");
        RequestHandler handler = WebManager.getHandler();
        handler.addAndDispatch(new RequestBuilder(SERVICES_JSON_URL, "maps-services")
                .cacheTo(new File(mapDirectory, "services.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<ServiceProfile>>() {}.getType();

                    List<ServiceProfile> serviceList = GSON.fromJson(json, type);
                    for (var service : serviceList) {
                        ServiceKind kind = ServiceKind.fromString(service.type);
                        if (kind != null) {
                            for (MapLocation location : service.locations) {
                                allPois.add(new ServicePoi(location, kind));
                            }
                        } else {
                            WynntilsMod.error("Unknown service type in services.json: " + service.type);
                        }
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

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.utils.BoundingBox;
import com.wynntils.wynn.model.map.poi.Label;
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.LostSpiritPoi;
import com.wynntils.wynn.model.map.poi.MapLocation;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MapModel extends Model {
    private static final String PLACES_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";
    private static final String SERVICES_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";
    private static final String MAPS_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json";
    private static final String SPIRITS_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/spirits.json";

    private static final Gson GSON = new GsonBuilder().create();
    private static final List<MapTexture> MAPS = new CopyOnWriteArrayList<>();
    private static final Set<LabelPoi> LABEL_POIS = new HashSet<>();
    private static final Set<ServicePoi> SERVICE_POIS = new HashSet<>();

    public static void init() {
        loadMaps();
        loadPlaces();
        loadServices();
    }

    public static Set<LabelPoi> getLabelPois() {
        return LABEL_POIS;
    }

    public static Set<ServicePoi> getServicePois() {
        return SERVICE_POIS;
    }

    public static List<MapTexture> getMapsForBoundingBox(BoundingBox box) {
        return MAPS.stream().filter(map -> box.intersects(map.getBox())).toList();
    }

    private static void loadMaps() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");
        RequestHandler handler = WebManager.getHandler();

        MAPS.clear();

        handler.addAndDispatch(new RequestBuilder(MAPS_JSON_URL, "map-parts")
                .cacheTo(new File(mapDirectory, "maps.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<MapPartProfile>>() {}.getType();

                    List<MapPartProfile> mapPartList = GSON.fromJson(json, type);
                    for (MapPartProfile mapPart : mapPartList) {
                        String fileName = mapPart.md5 + ".png";

                        handler.addRequest(new RequestBuilder(mapPart.url, "map-part-" + mapPart.name)
                                .cacheTo(new File(mapDirectory, fileName))
                                .cacheMD5Validator(mapPart.md5)
                                .useCacheAsBackup()
                                .handle(bytes -> {
                                    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                                        NativeImage nativeImage = NativeImage.read(in);
                                        MapTexture mapPartImage = new MapTexture(
                                                fileName, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                                        MAPS.add(mapPartImage);
                                    } catch (IOException e) {
                                        WynntilsMod.info(
                                                "IOException occurred while loading map image of " + mapPart.name);
                                        return false; // don't cache
                                    }

                                    return true;
                                })
                                .build());
                    }

                    handler.dispatchAsync();
                    return true;
                })
                .build());
    }

    private static void loadPlaces() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");
        RequestHandler handler = WebManager.getHandler();
        handler.addAndDispatch(new RequestBuilder(PLACES_JSON_URL, "maps-places")
                .cacheTo(new File(mapDirectory, "places.json"))
                .useCacheAsBackup()
                .handleJsonObject(json -> {
                    PlacesProfile places = GSON.fromJson(json, PlacesProfile.class);
                    for (Label label : places.labels) {
                        LABEL_POIS.add(new LabelPoi(label));
                    }
                    return true;
                })
                .build());
    }

    private static void loadServices() {
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
                                SERVICE_POIS.add(new ServicePoi(location, kind));
                            }
                        } else {
                            WynntilsMod.warn("Unknown service type in services.json: " + service.type);
                        }
                    }

                    return true;
                })
                .build());

        handler.addAndDispatch(new RequestBuilder(SPIRITS_JSON_URL, "maps-spirits")
                .cacheTo(new File(mapDirectory, "spirits.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<MapLocation>>() {}.getType();

                    List<MapLocation> mapLocations = GSON.fromJson(json, type);

                    for (int i = 0; i < mapLocations.size(); i++) {
                        SERVICE_POIS.add(new LostSpiritPoi(mapLocations.get(i), i + 1));
                    }

                    return true;
                })
                .build());
    }

    private static class PlacesProfile {
        List<Label> labels;
    }

    private static class ServiceProfile {
        String type;
        List<MapLocation> locations;
    }

    private static class MapPartProfile {
        final String name;
        final String url;
        final int x1;
        final int z1;
        final int x2;
        final int z2;
        final String md5;

        private MapPartProfile(String name, String url, int x1, int z1, int x2, int z2, String md5) {
            this.name = name;
            this.url = url;
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
            this.md5 = md5;
        }
    }
}

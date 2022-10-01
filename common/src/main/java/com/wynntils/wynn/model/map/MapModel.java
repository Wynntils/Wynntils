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
import com.wynntils.mc.objects.Location;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.map.poi.Label;
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.MapLocation;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import com.wynntils.wynn.model.map.poi.WaypointPoi;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class MapModel extends Model {
    private static final String PLACES_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";
    private static final String SERVICES_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";
    private static final String MAPS_JSON_URL =
            "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json";

    private static final Gson GSON = new GsonBuilder().create();
    private static List<MapTexture> maps = Collections.synchronizedList(new ArrayList());
    private static final Set<Poi> allPois = new HashSet<>();

    public static void init() {
        loadMaps();
        loadPlaces();
        loadServices();
    }

    public static Optional<MapTexture> getMapForLocation(int x, int z) {
        return maps.stream().filter(map -> MathUtils.isInside(x, z, map.getX1(), map.getX2(), map.getZ1(), map.getZ2())).findFirst();
    }

    public static Stream<Poi> getAllPois() {
        if (CompassModel.getCompassLocation().isPresent()) {
            Location location = CompassModel.getCompassLocation().get();
            // Always render waypoint POI on top
            WaypointPoi waypointPoi =
                    new WaypointPoi(new MapLocation((int) location.x, Integer.MAX_VALUE, (int) location.z));

            return Stream.concat(allPois.stream(), Stream.of(waypointPoi));
        }

        return allPois.stream();
    }

    private static void loadMaps() {
        File mapDirectory = new File(WebManager.API_CACHE_ROOT, "maps");
        RequestHandler handler = WebManager.getHandler();
        String mapPartUrlBase = MAPS_JSON_URL.substring(0, MAPS_JSON_URL.lastIndexOf("/") + 1);

        maps.clear();

        handler.addAndDispatch(new RequestBuilder(MAPS_JSON_URL, "map-parts")
                .cacheTo(new File(mapDirectory, "maps.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<MapPartProfile>>() {}.getType();

                    List<MapPartProfile> mapPartList = GSON.fromJson(json, type);
                    for (MapPartProfile mapPart : mapPartList) {
                        String mapPartUrl = mapPartUrlBase + mapPart.file;

                        handler.addRequest(new RequestBuilder(mapPartUrl, "map-part-" + mapPart.file)
                                .cacheTo(new File(mapDirectory, mapPart.file))
                                .cacheMD5Validator(mapPart.md5)
                                .useCacheAsBackup()
                                .handle(bytes -> {
                                    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                                        NativeImage nativeImage = NativeImage.read(in);
                                        MapTexture mapPartImage = new MapTexture(mapPart.file, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                                        maps.add(mapPartImage);
                                    } catch (IOException e) {
                                        WynntilsMod.info("IOException occurred while loading map image of " + mapPart.file);
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
                        allPois.add(new LabelPoi(label));
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

    private static class PlacesProfile {
        List<Label> labels;
    }

    private static class ServiceProfile {
        String type;
        List<MapLocation> locations;
    }

    private static class MapPartProfile {
        final String file;
        final int x1;
        final int z1;
        final int x2;
        final int z2;
        final String md5;

        public MapPartProfile(String file, int x1, int z1, int x2, int z2, String md5) {
            this.file = file;
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
            this.md5 = md5;
        }
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.BoundingBox;
import com.wynntils.wynn.model.map.poi.CombatKind;
import com.wynntils.wynn.model.map.poi.CombatPoi;
import com.wynntils.wynn.model.map.poi.Label;
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.PoiLocation;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MapModel extends Model {
    private final List<MapTexture> maps = new CopyOnWriteArrayList<>();
    private final Set<LabelPoi> labelPois = new HashSet<>();
    private final Set<ServicePoi> servicePois = new HashSet<>();
    private final Set<CombatPoi> combatPois = new HashSet<>();

    @Override
    public void init() {
        loadMaps();
        loadPlaces();
        loadServices();
        loadCombat();
    }

    public Set<LabelPoi> getLabelPois() {
        return labelPois;
    }

    public Set<ServicePoi> getServicePois() {
        return servicePois;
    }

    public Set<CombatPoi> getCombatPois() {
        return combatPois;
    }

    public List<MapTexture> getMapsForBoundingBox(BoundingBox box) {
        return maps.stream().filter(map -> box.intersects(map.getBox())).toList();
    }

    private void loadMaps() {
        maps.clear();

        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MAPS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<MapPartProfile>>() {}.getType();

            List<MapPartProfile> mapPartList = WynntilsMod.GSON.fromJson(reader, type);
            for (MapPartProfile mapPart : mapPartList) {
                String fileName = mapPart.md5 + ".png";

                Download dlPart = Managers.Net.download(URI.create(mapPart.url), "maps/" + fileName, mapPart.md5);
                dlPart.handleInputStream(
                        inputStream -> {
                            try {
                                NativeImage nativeImage = NativeImage.read(inputStream);
                                MapTexture mapPartImage = new MapTexture(
                                        fileName, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                                maps.add(mapPartImage);
                            } catch (IOException e) {
                                WynntilsMod.warn("IOException occurred while loading map image of " + mapPart.name, e);
                            }
                        },
                        onError -> WynntilsMod.warn(
                                "Error occurred while download map image of " + mapPart.name, onError));
            }
        });
    }

    private void loadPlaces() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_PLACES);
        dl.handleReader(reader -> {
            PlacesProfile places = WynntilsMod.GSON.fromJson(reader, PlacesProfile.class);
            for (Label label : places.labels) {
                labelPois.add(new LabelPoi(label));
            }
        });
    }

    private void loadServices() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_SERVICES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<ServiceProfile>>() {}.getType();

            List<ServiceProfile> serviceList = WynntilsMod.GSON.fromJson(reader, type);
            for (var service : serviceList) {
                ServiceKind kind = ServiceKind.fromString(service.type);
                if (kind != null) {
                    for (PoiLocation location : service.locations) {
                        servicePois.add(new ServicePoi(location, kind));
                    }
                } else {
                    WynntilsMod.warn("Unknown service type in services.json: " + service.type);
                }
            }
        });
    }

    private void loadCombat() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_COMBAT_LOCATIONS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<CombatProfileList>>() {}.getType();

            List<CombatProfileList> combatProfileLists = WynntilsMod.GSON.fromJson(reader, type);
            for (var combatList : combatProfileLists) {
                CombatKind kind = CombatKind.fromString(combatList.type);
                if (kind != null) {
                    for (CombatProfileList.CombatProfile profile : combatList.locations) {
                        combatPois.add(new CombatPoi(profile.coordinates, profile.name, kind));
                    }
                } else {
                    WynntilsMod.warn("Unknown combat type in combat.json: " + combatList.type);
                }
            }
        });
    }

    private static class PlacesProfile {
        List<Label> labels;
    }

    private static class ServiceProfile {
        String type;
        List<PoiLocation> locations;
    }

    private static class CombatProfileList {
        String type;
        List<CombatProfile> locations;

        private static class CombatProfile {
            String name;
            PoiLocation coordinates;
        }
    }

    private static final class MapPartProfile {
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

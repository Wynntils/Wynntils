/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.event.NetResultProcessedEvent;
import com.wynntils.models.map.pois.CombatPoi;
import com.wynntils.models.map.pois.LabelPoi;
import com.wynntils.models.map.pois.ServicePoi;
import com.wynntils.models.map.type.CombatKind;
import com.wynntils.models.map.type.ServiceKind;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.utils.type.BoundingBox;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MapModel extends Model {
    private final List<MapTexture> maps = new CopyOnWriteArrayList<>();
    private final Set<LabelPoi> labelPois = new HashSet<>();
    private final Set<ServicePoi> servicePois = new HashSet<>();
    private final Set<CombatPoi> combatPois = new HashSet<>();

    public MapModel(GuildAttackTimerModel guildAttackTimerModel) {
        super(List.of(guildAttackTimerModel));

        loadData();
    }

    @Override
    public void reloadData() {
        loadData();
    }

    private void loadData() {
        loadMaps();
        loadPlaces();
        // Slightly hacky way to reduce risk of class loading race in development environment
        // These are loaded serially after places instad
        if (WynntilsMod.isDevelopmentEnvironment()) return;

        loadServices();
        loadCombat();
    }

    @SubscribeEvent
    public void onDataLoaded(NetResultProcessedEvent.ForUrlId event) {
        if (!WynntilsMod.isDevelopmentEnvironment()) return;
        // Serialize loading of POIs when on dev env

        if (event.getUrlId() == UrlId.DATA_STATIC_PLACES) {
            loadServices();
            return;
        }
        if (event.getUrlId() == UrlId.DATA_STATIC_SERVICES) {
            loadCombat();
            return;
        }
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

                loadMapPart(mapPart, fileName);
            }
        });
    }

    private void loadMapPart(MapPartProfile mapPart, String fileName) {
        Download dl = Managers.Net.download(URI.create(mapPart.url), "maps/" + fileName, mapPart.md5);
        dl.handleInputStream(
                inputStream -> {
                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        MapTexture mapPartImage =
                                new MapTexture(fileName, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                        maps.add(mapPartImage);
                    } catch (IOException e) {
                        WynntilsMod.warn("IOException occurred while loading map image of " + mapPart.name, e);
                    }
                },
                onError -> WynntilsMod.warn("Error occurred while download map image of " + mapPart.name, onError));
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
                    for (CombatProfile profile : combatList.locations) {
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

    private static class CombatProfile {
        String name;
        PoiLocation coordinates;
    }

    private static class CombatProfileList {
        String type;
        List<CombatProfile> locations;
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

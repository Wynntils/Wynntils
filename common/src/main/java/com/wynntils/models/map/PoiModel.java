/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.event.NetResultProcessedEvent;
import com.wynntils.core.storage.Storage;
import com.wynntils.models.map.pois.CombatPoi;
import com.wynntils.models.map.pois.CustomPoi;
import com.wynntils.models.map.pois.LabelPoi;
import com.wynntils.models.map.pois.ServicePoi;
import com.wynntils.models.map.type.CombatKind;
import com.wynntils.models.map.type.CustomPoiProvider;
import com.wynntils.models.map.type.ServiceKind;
import com.wynntils.utils.render.Texture;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PoiModel extends Model {
    public static final List<Texture> POI_ICONS = List.of(
            Texture.FLAG,
            Texture.DIAMOND,
            Texture.FIREBALL,
            Texture.SIGN,
            Texture.STAR,
            Texture.WALL,
            Texture.CHEST_T1,
            Texture.CHEST_T2,
            Texture.CHEST_T3,
            Texture.CHEST_T4,
            Texture.FARMING,
            Texture.FISHING,
            Texture.MINING,
            Texture.WOODCUTTING);

    private final Set<LabelPoi> labelPois = new HashSet<>();
    private final Set<ServicePoi> servicePois = new HashSet<>();
    private final Set<CombatPoi> combatPois = new HashSet<>();
    private final Map<CustomPoiProvider, Set<CustomPoi>> providedCustomPois = new ConcurrentHashMap<>();

    private Storage<List<CustomPoiProvider>> customPoiProviders = new Storage<>(new ArrayList<>());

    public PoiModel() {
        super(List.of());

        loadData();
    }

    @Override
    public void reloadData() {
        loadData();
    }

    @Override
    public void onStorageLoad() {
        loadCustomPoiProviders();
    }

    private void loadData() {
        loadPlaces();
        // Slightly hacky way to reduce risk of class loading race in development environment
        // These are loaded serially after places instad
        if (WynntilsMod.isDevelopmentEnvironment()) return;

        loadServices();
        loadCombat();
        loadCustomPoiProviders();
    }

    public void loadCustomPoiProviders() {
        for (CustomPoiProvider poiProvider : customPoiProviders.get()) {
            Managers.Net.download(poiProvider.getUrl(), poiProvider.getName()).handleJsonArray(elements -> {
                Set<CustomPoi> pois = new HashSet<>();

                for (JsonElement jsonElement : elements) {
                    CustomPoi poi = Managers.Json.GSON.fromJson(jsonElement, CustomPoi.class);
                    pois.add(poi);
                }

                providedCustomPois.put(poiProvider, pois);
            });
        }
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
        if (event.getUrlId() == UrlId.DATA_STATIC_COMBAT_LOCATIONS) {
            loadCustomPoiProviders();
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

    public Set<CustomPoi> getProvidedCustomPois() {
        return customPoiProviders.get().stream()
                .filter(CustomPoiProvider::isEnabled)
                .map(provider -> providedCustomPois.getOrDefault(provider, Set.of()))
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<CustomPoiProvider> getCustomPoiProviders() {
        return customPoiProviders.get();
    }

    public void addCustomPoiProvider(CustomPoiProvider poiProvider) {
        customPoiProviders.get().add(poiProvider);
        customPoiProviders.touched();
        loadCustomPoiProviders();
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

    public boolean removeCustomPoiProvider(String name) {
        Optional<CustomPoiProvider> provider = customPoiProviders.get().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();

        if (provider.isEmpty()) return false;

        customPoiProviders.get().remove(provider.get());
        providedCustomPois.remove(provider.get());

        return true;
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
}

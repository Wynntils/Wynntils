/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.map.pois.CombatPoi;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.map.pois.LabelPoi;
import com.wynntils.services.map.pois.ServicePoi;
import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.map.type.CustomPoiProvider;
import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.services.mapdata.providers.builtin.CombatListProvider;
import com.wynntils.services.mapdata.providers.builtin.PlaceListProvider;
import com.wynntils.services.mapdata.providers.builtin.ServiceListProvider;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PoiService extends Service {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Label.class, new Label.LabelDeserializer())
            .enableComplexMapKeySerialization()
            .create();

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
    private final Set<CombatPoi> cavePois = new HashSet<>();
    private final Map<CustomPoiProvider, List<CustomPoi>> providedCustomPois = new ConcurrentHashMap<>();

    @Persisted
    private final Storage<List<CustomPoiProvider>> customPoiProviders = new Storage<>(new ArrayList<>());

    public PoiService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_PLACES).handleReader(this::handlePlaces);
        registry.registerDownload(UrlId.DATA_STATIC_SERVICES).handleReader(this::handleServices);
        registry.registerDownload(UrlId.DATA_STATIC_COMBAT_LOCATIONS).handleReader(this::handleCombat);
        registry.registerDownload(UrlId.DATA_STATIC_CAVE_INFO).handleReader(this::handleCaves);
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        loadCustomPoiProviders();
    }

    public Stream<LabelPoi> getLabelPois() {
        return labelPois.stream();
    }

    public Stream<ServicePoi> getServicePois() {
        return servicePois.stream();
    }

    public Stream<CombatPoi> getCombatPois() {
        return Stream.concat(combatPois.stream(), cavePois.stream());
    }

    public List<CustomPoi> getProvidedCustomPois() {
        return customPoiProviders.get().stream()
                .filter(CustomPoiProvider::isEnabled)
                .map(provider -> providedCustomPois.getOrDefault(provider, List.of()))
                .flatMap(List::stream)
                .toList();
    }

    public List<CustomPoiProvider> getCustomPoiProviders() {
        return customPoiProviders.get();
    }

    public void addCustomPoiProvider(CustomPoiProvider poiProvider) {
        customPoiProviders.get().add(poiProvider);
        customPoiProviders.touched();
        loadCustomPoiProviders();
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

    public boolean isPoiProvided(CustomPoi customPoi) {
        return getProvidedCustomPois().contains(customPoi);
    }

    private void handlePlaces(Reader reader) {
        PlacesProfile places = GSON.fromJson(reader, PlacesProfile.class);
        for (Label label : places.labels) {
            labelPois.add(new LabelPoi(label));
            PlaceListProvider.registerFeature(label);
        }
    }

    private void handleServices(Reader reader) {
        Type type = new TypeToken<List<ServiceProfile>>() {}.getType();

        List<ServiceProfile> serviceList = GSON.fromJson(reader, type);
        for (ServiceProfile service : serviceList) {
            ServiceKind kind = ServiceKind.fromString(service.type);
            if (kind != null) {
                for (PoiLocation location : service.locations) {
                    servicePois.add(new ServicePoi(location, kind));
                    ServiceListProvider.registerFeature(new Location(location), kind);
                }
            } else {
                WynntilsMod.warn("Unknown service type in services.json: " + service.type);
            }
        }
    }

    private void handleCombat(Reader reader) {
        Type type = new TypeToken<List<CombatProfileList>>() {}.getType();

        List<CombatProfileList> combatProfileLists = GSON.fromJson(reader, type);
        for (CombatProfileList combatList : combatProfileLists) {
            CombatKind kind = CombatKind.fromString(combatList.type);
            // We load caves separately... until the refactor
            if (kind != null && kind != CombatKind.CAVES) {
                for (CombatProfile profile : combatList.locations) {
                    combatPois.add(new CombatPoi(profile.coordinates, profile.name, kind));
                    CombatListProvider.registerFeature(new Location(profile.coordinates), kind, profile.name);
                }
            } else {
                WynntilsMod.warn("Unknown combat type in combat.json: " + combatList.type);
            }
        }
    }

    private void handleCaves(Reader reader) {
        Type type = new TypeToken<List<CaveProfile>>() {}.getType();

        List<CaveProfile> profiles = GSON.fromJson(reader, type);

        cavePois.addAll(profiles.stream()
                .map(profile -> {
                    CombatListProvider.registerFeature(profile.location, CombatKind.CAVES, profile.name);
                    return new CombatPoi(PoiLocation.fromLocation(profile.location), profile.name, CombatKind.CAVES);
                })
                .collect(Collectors.toUnmodifiableSet()));
    }

    public void loadCustomPoiProviders() {
        for (CustomPoiProvider poiProvider : customPoiProviders.get()) {
            Managers.Net.download(poiProvider.getUrl(), poiProvider.getName()).handleJsonArray(elements -> {
                List<CustomPoi> pois = new ArrayList<>();

                for (JsonElement jsonElement : elements) {
                    CustomPoi poi = GSON.fromJson(jsonElement, CustomPoi.class);
                    pois.add(poi);
                }

                providedCustomPois.put(poiProvider, ImmutableList.copyOf(pois));
            });
        }
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

    private record CaveProfile(
            String type,
            String name,
            String specialInfo,
            String description,
            String length,
            String lengthInfo,
            String difficulty,
            int requirements,
            List<String> rewards,
            Location location) {}
}

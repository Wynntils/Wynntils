/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.mapdata.attributes.resolving.MapAttributesResolver;
import com.wynntils.services.mapdata.attributes.resolving.OverrideMapAttributes;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapVisibility;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.builtin.CategoriesProvider;
import com.wynntils.services.mapdata.providers.builtin.CombatListProvider;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.services.mapdata.providers.builtin.PlaceListProvider;
import com.wynntils.services.mapdata.providers.builtin.ServiceListProvider;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.providers.type.MapDataOverrideProvider;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MapDataService extends Service {
    private static final CategoriesProvider CATEGORIES_PROVIDER = new CategoriesProvider();
    private static final MapIconsProvider MAP_ICONS_PROVIDER = new MapIconsProvider();
    private static final ServiceListProvider SERVICE_LIST_PROVIDER = new ServiceListProvider();
    private static final CombatListProvider COMBAT_LIST_PROVIDER = new CombatListProvider();
    private static final PlaceListProvider PLACE_LIST_PROVIDER = new PlaceListProvider();

    private static final MapDataProvider ONLINE_PLACEHOLDER_PROVIDER = new PlaceholderProvider();
    // FIXME: i18n
    private static final String NAMELESS_CATEGORY = "Category '%s'";

    private final LinkedHashMap<String, MapDataProvider> allProviders = new LinkedHashMap<>();
    private final LinkedHashMap<String, MapDataOverrideProvider> overrideProviders = new LinkedHashMap();

    // Cache for resolved attributes and icons
    private final Map<MapFeature, ResolvedMapAttributes> resolvedAttributesCache = new HashMap<>();
    private final Map<String, Optional<MapIcon>> iconCache = new HashMap<>();

    // Storage for json providers
    @Persisted
    private final Storage<Map<JsonProviderInfo, Boolean>> jsonProviderInfos = new Storage<>(new LinkedHashMap<>());

    // FIXME: These should be a HiddenConfig when configs are supported here
    @Persisted
    private final Storage<Set<String>> filteredMapCategories = new Storage<>(new HashSet<>());

    @Persisted
    private final Storage<Set<String>> filteredMinimapCategories = new Storage<>(new HashSet<>());

    public MapDataService() {
        super(List.of());

        createBuiltInProviders();
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        if (storage == jsonProviderInfos) {
            reloadJsonProviders();
        }
    }

    @Override
    public void reloadData() {
        getProviders().forEach(MapDataProvider::reloadData);
        reloadJsonProviders();
    }

    public Stream<MapFeature> getFeatures() {
        return getProviders().flatMap(MapDataProvider::getFeatures);
    }

    public Stream<MapIcon> getIcons() {
        return getProviders().flatMap(MapDataProvider::getIcons);
    }

    public Stream<MapFeature> getFeaturesForCategory(String categoryId) {
        return getFeatures().filter(f -> f.getCategoryId().startsWith(categoryId));
    }

    // region Lookup features and attribute resolution

    public ResolvedMapAttributes resolveMapAttributes(MapFeature feature) {
        return resolvedAttributesCache.computeIfAbsent(feature, k -> MapAttributesResolver.resolve(feature));
    }

    public Stream<MapCategory> getCategoryDefinitions(String categoryId) {
        return getProviders().flatMap(MapDataProvider::getCategories).filter(p -> p.getCategoryId()
                .equals(categoryId));
    }

    public String getCategoryName(String categoryId) {
        return getCategoryDefinitions(categoryId)
                .map(MapCategory::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(NAMELESS_CATEGORY.formatted(categoryId));
    }

    public Optional<MapIcon> getIcon(String iconId) {
        if (iconId.equals(MapIcon.NO_ICON_ID)) return Optional.empty();

        return iconCache.computeIfAbsent(iconId, k -> {
            Stream<MapIcon> allIcons = getProviders().flatMap(MapDataProvider::getIcons);
            return allIcons.filter(i -> i.getIconId().equals(iconId)).findFirst();
        });
    }

    public Optional<MapIcon> getIcon(MapFeature feature) {
        return getIcon(resolveMapAttributes(feature).iconId());
    }

    public MapIcon getIconOrFallback(String iconId) {
        return getIcon(iconId)
                .orElse(Services.MapData.getIcon(MapIconsProvider.FALLBACK_ICON_ID)
                        .get());
    }

    public MapIcon getIconOrFallback(MapFeature feature) {
        return getIcon(feature)
                .orElse(Services.MapData.getIcon(MapIconsProvider.FALLBACK_ICON_ID)
                        .get());
    }

    public Optional<MapAttributes> getOverrideAttributesForFeature(MapFeature feature) {
        return OverrideMapAttributes.from(Stream.concat(
                        overrideProviders.values().stream().filter(attr -> attr.getOverridenFeatureIds()
                                .anyMatch(attrFeatureId -> attrFeatureId.equals(feature.getFeatureId()))),
                        overrideProviders.values().stream().filter(attr -> attr.getOverridenCategoryIds()
                                .anyMatch(attrCategoryId ->
                                        feature.getCategoryId().startsWith(attrCategoryId))))
                .map(provider -> provider.getOverrideAttributes(feature))
                .toList());
    }

    // endregion

    // region Providers

    // In the future, per-account, per-character or shared
    // can be added just from disk, or downloaded from an url
    public void addJsonProvider(JsonProviderInfo providerInfo) {
        jsonProviderInfos.get().keySet().removeIf(i -> i.providerId().equals(providerInfo.providerId()));
        jsonProviderInfos.get().put(providerInfo, true);
        jsonProviderInfos.touched();
        registerJsonProvider(providerInfo);
    }

    public boolean removeJsonProvider(String providerId) {
        boolean found = jsonProviderInfos.get().keySet().removeIf(info -> info.providerId()
                .equals(providerId));
        if (!found) return false;

        jsonProviderInfos.touched();
        allProviders.keySet().stream()
                .filter(id -> id.endsWith(providerId))
                .findFirst()
                .ifPresent(allProviders::remove);

        return true;
    }

    public boolean toggleJsonProvider(String providerId) {
        Optional<JsonProviderInfo> providerOpt = jsonProviderInfos.get().keySet().stream()
                .filter(info -> info.providerId().equals(providerId))
                .findFirst();
        if (providerOpt.isEmpty()) return false;

        JsonProviderInfo provider = providerOpt.get();
        boolean enabled = jsonProviderInfos.get().get(provider);
        jsonProviderInfos.get().put(provider, !enabled);
        jsonProviderInfos.touched();

        if (enabled) {
            allProviders.entrySet().stream()
                    .filter(entry -> entry.getKey().endsWith(providerId))
                    .findFirst()
                    .ifPresent(entry -> {
                        allProviders.remove(entry.getKey());
                        invalidateCaches(entry.getValue());
                    });
        } else {
            registerJsonProvider(provider);
        }

        return true;
    }

    public boolean isJsonProviderEnabled(String providerId) {
        return jsonProviderInfos.get().keySet().stream()
                .filter(info -> info.providerId().equals(providerId))
                .findFirst()
                .map(jsonProviderInfos.get()::get)
                .orElse(false);
    }

    public Map<JsonProviderInfo, Boolean> getJsonProviderInfos() {
        return Collections.unmodifiableMap(jsonProviderInfos.get());
    }

    public void registerOverrideProvider(String overrideProviderId, MapDataOverrideProvider provider) {
        overrideProviders.putFirst(overrideProviderId, provider);
        provider.onChange(this::onProviderChange);

        // Invalidate caches for the features that this provider overrides
        this.getFeatures()
                .filter(feature -> provider.getOverridenCategoryIds()
                                .anyMatch(categoryId -> feature.getCategoryId().startsWith(categoryId))
                        || provider.getOverridenFeatureIds().anyMatch(feature.getFeatureId()::equals))
                .forEach(this::onProviderChange);
    }

    public void unregisterOverrideProvider(String overrideProviderId) {
        MapDataOverrideProvider provider = overrideProviders.remove(overrideProviderId);
        if (provider == null) return;

        // Invalidate caches for the features that this provider overrides
        this.getFeatures()
                .filter(feature -> provider.getOverridenCategoryIds()
                                .anyMatch(categoryId -> feature.getCategoryId().startsWith(categoryId))
                        || provider.getOverridenFeatureIds().anyMatch(feature.getFeatureId()::equals))
                .forEach(this::onProviderChange);
    }

    /**
     * Register a built-in provider. Call this method in a listener for {@link com.wynntils.core.mod.event.WynntilsInitEvent.ModInitFinished}, unless you have a good reason not to.
     *
     * @param provider The provider to register
     */
    public void registerBuiltInProvider(BuiltInProvider provider) {
        registerProvider("built-in:" + provider.getProviderId(), provider);
        WynntilsMod.registerEventListener(provider);
    }

    private void createBuiltInProviders() {
        // Metadata
        registerBuiltInProvider(CATEGORIES_PROVIDER);
        registerBuiltInProvider(MAP_ICONS_PROVIDER);

        // Locations
        registerBuiltInProvider(SERVICE_LIST_PROVIDER);
        registerBuiltInProvider(COMBAT_LIST_PROVIDER);
        registerBuiltInProvider(PLACE_LIST_PROVIDER);
    }

    private void registerProvider(String providerId, MapDataProvider provider) {
        if (provider == null) {
            WynntilsMod.warn("Provider missing for '" + providerId + "'");
            return;
        }
        // Add or update the provider
        allProviders.putFirst(providerId, provider);
        provider.onChange(this::onProviderChange);

        // Invalidate caches
        invalidateCaches(provider);
    }

    private void registerJsonProvider(JsonProviderInfo providerInfo) {
        switch (providerInfo.providerType()) {
            case BUNDLED -> createBundledProvider(providerInfo.providerId(), providerInfo.providerFilename());
            case LOCAL -> createLocalProvider(providerInfo.providerId(), providerInfo.providerFilePath());
            case REMOTE -> createOnlineProvider(providerInfo.providerId(), providerInfo.providerUrl());
        }
    }

    private void createBundledProvider(String id, String filename) {
        String completeId = "bundled:" + id;
        JsonProvider provider = JsonProvider.loadBundledResource(completeId, filename);
        registerProvider(completeId, provider);
    }

    private void createLocalProvider(String id, String filename) {
        String completeId = "local:" + id;
        JsonProvider provider = JsonProvider.loadLocalFile(completeId, new File(filename));
        registerProvider(completeId, provider);
    }

    private void createOnlineProvider(String id, String url) {
        String completeId = "online:" + id;
        // Register a dummy provider; this will be replaced once loading has finished
        registerProvider(completeId, ONLINE_PLACEHOLDER_PROVIDER);
        JsonProvider.loadOnlineResource(completeId, url, this::registerProvider);
    }

    private void reloadJsonProviders() {
        jsonProviderInfos.get().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .forEach(this::registerJsonProvider);
    }

    private void onProviderChange(MapDataProvidedType mapDataProvidedType) {
        if (mapDataProvidedType instanceof MapFeature mapFeature) {
            resolvedAttributesCache.remove(mapFeature);
        } else if (mapDataProvidedType instanceof MapIcon mapIcon) {
            iconCache.remove(mapIcon.getIconId());
        } else if (mapDataProvidedType instanceof MapCategory mapCategory) {
            resolvedAttributesCache.keySet().removeIf(feature -> feature.getCategoryId()
                    .startsWith(mapCategory.getCategoryId()));
        }
    }

    private void invalidateCaches(MapDataProvider provider) {
        // As only the icon id string is cached, there is no need to invalidate the cache for features that contain
        // this provider's icons. It's enough to invalidate the icon cache below, and the icon lookup will default to
        // the correct icon.
        List<MapFeature> toBeRemoved = resolvedAttributesCache.keySet().stream()
                .filter(feature ->
                        provider.getFeatures().anyMatch(f -> f.getFeatureId().equals(feature.getFeatureId()))
                                || provider.getCategories()
                                        .anyMatch(c -> feature.getCategoryId().startsWith(c.getCategoryId())))
                .toList();
        resolvedAttributesCache.keySet().removeAll(toBeRemoved);

        List<String> toBeRemovedIcons = iconCache.keySet().stream()
                .filter(iconId ->
                        provider.getIcons().anyMatch(i -> i.getIconId().equals(iconId)))
                .toList();
        iconCache.keySet().removeAll(toBeRemovedIcons);
    }

    private Stream<MapDataProvider> getProviders() {
        return allProviders.values().stream();
    }

    // endregion

    // region map filtering
    public void filterMapCategory(String category) {
        if (!isCategoryFilteredOnMap(category)) {
            filteredMapCategories.get().remove(category);
        } else {
            filteredMapCategories.get().add(category);
        }
        filteredMapCategories.touched();
    }

    public boolean isCategoryFilteredOnMap(String category) {
        return !filteredMapCategories.get().contains(category);
    }

    public void filterMinimapCategory(String category) {
        if (!isCategoryFilteredOnMinimap(category)) {
            filteredMinimapCategories.get().remove(category);
        } else {
            filteredMinimapCategories.get().add(category);
        }
        filteredMinimapCategories.touched();
    }

    public boolean isCategoryFilteredOnMinimap(String category) {
        return !filteredMinimapCategories.get().contains(category);
    }
    // endregion

    /**
     * This method requires a MapVisibility with all values non-empty to work correctly.
     */
    public float calculateVisibility(ResolvedMapVisibility mapVisibility, float zoomLevel) {
        float min = mapVisibility.min();
        float max = mapVisibility.max();
        float fade = mapVisibility.fade();

        // Having max larger than min is a way to represent "never show"
        if (max < min) {
            return 0f;
        }

        float startFadeIn = min - fade;
        float stopFadeIn = min + fade;
        float startFadeOut = max - fade;
        float stopFadeOut = max + fade;

        // If min or max is at the extremes, do not apply fading.
        // This is a way of representing "always show".
        if (min <= 1) {
            startFadeIn = 0;
            stopFadeIn = 0;
        }
        if (max >= 100) {
            startFadeOut = 101;
            stopFadeOut = 101;
        }

        if (zoomLevel < startFadeIn) {
            return 0;
        }
        if (zoomLevel < stopFadeIn) {
            // The visibility should be linearly interpolated between 0 and 1 for values
            // between startFadeIn and stopFadeIn.
            return (zoomLevel - startFadeIn) / (fade * 2);
        }

        if (zoomLevel < startFadeOut) {
            return 1;
        }

        if (zoomLevel < stopFadeOut) {
            // The visibility should be linearly interpolated between 1 and 0 for values
            // between startFadeIn and stopFadeIn.
            return 1 - (zoomLevel - startFadeOut) / (fade * 2);
        }

        return 0;
    }

    public double calculateMarkerVisibility(Location location, ResolvedMarkerOptions resolvedMarkerOptions) {
        double distanceToPlayer =
                Math.sqrt(location.distanceToSqr(McUtils.player().position()));

        double startFadeInDistance = resolvedMarkerOptions.maxDistance() + resolvedMarkerOptions.fade();
        double stopFadeInDistance = resolvedMarkerOptions.maxDistance() - resolvedMarkerOptions.fade();
        float startFadeOutDistance = resolvedMarkerOptions.minDistance() + resolvedMarkerOptions.fade();
        float stopFadeOutDistance = resolvedMarkerOptions.minDistance() - resolvedMarkerOptions.fade();

        if (distanceToPlayer > startFadeInDistance) {
            return 0;
        }

        if (distanceToPlayer > stopFadeInDistance) {
            return 1 - (distanceToPlayer - stopFadeInDistance) / (resolvedMarkerOptions.fade() * 2);
        }

        if (distanceToPlayer > startFadeOutDistance) {
            return 1;
        }

        if (distanceToPlayer > stopFadeOutDistance) {
            return (distanceToPlayer - stopFadeOutDistance) / (resolvedMarkerOptions.fade() * 2);
        }

        return 0;
    }

    private static final class PlaceholderProvider implements MapDataProvider {
        @Override
        public Stream<MapFeature> getFeatures() {
            return Stream.empty();
        }

        @Override
        public Stream<MapCategory> getCategories() {
            return Stream.empty();
        }

        @Override
        public Stream<MapIcon> getIcons() {
            return Stream.empty();
        }

        @Override
        public void onChange(Consumer<MapDataProvidedType> callback) {}

        @Override
        public void reloadData() {}
    }
}

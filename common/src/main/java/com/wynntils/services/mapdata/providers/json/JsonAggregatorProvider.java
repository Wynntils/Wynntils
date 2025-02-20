/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A provider that aggregates multiple {@link JsonProvider}s. This is done so that {@link JsonProvider}s
 * can be loaded from multiple sources without having to be registered individually.
 */
public class JsonAggregatorProvider extends BuiltInProvider {
    private static final Set<JsonProvider> PROVIDED_PROVIDERS = new LinkedHashSet<>();

    @Override
    public String getProviderId() {
        return "json-aggregator";
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_PROVIDERS.stream().flatMap(JsonProvider::getCategories);
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_PROVIDERS.stream().flatMap(JsonProvider::getFeatures);
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return PROVIDED_PROVIDERS.stream().flatMap(JsonProvider::getIcons);
    }

    @Override
    public void reloadData() {
        // This provider does not explicitly need to handle reloading, as it is only a container for other providers.
        // Reloading is done by the owner core component.
    }

    public void updateProviders(List<JsonProvider> providers) {
        PROVIDED_PROVIDERS.stream()
                .filter(provider -> !providers.contains(provider))
                .forEach(provider -> {
                    provider.getCategories().forEach(this::notifyCallbacks);
                    provider.getFeatures().forEach(this::notifyCallbacks);
                    provider.getIcons().forEach(this::notifyCallbacks);
                });
        PROVIDED_PROVIDERS.removeIf(provider -> !providers.contains(provider));
        PROVIDED_PROVIDERS.addAll(providers);
    }
}

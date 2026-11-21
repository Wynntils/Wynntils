/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.MapDataOverrideProvider;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JsonOverrideProvider implements MapDataOverrideProvider, Comparable<JsonOverrideProvider> {
    // Note: The version field is not used, but is kept for future compatibility
    //       If the need arises, it can be used to handle different versions of the json format
    //       This is easily achieved by GSON switching to different deserializers based on the version
    private final int version;
    private final String providerId;
    private final MapAttributesImpl attributes;
    private final Set<String> featureIds;
    private final Set<String> categoryIds;

    public JsonOverrideProvider(
            int version,
            String providerId,
            MapAttributesImpl attributes,
            Set<String> featureIds,
            Set<String> categoryIds) {
        this.version = version;
        this.providerId = providerId;
        this.attributes = attributes;
        this.featureIds = featureIds;
        this.categoryIds = categoryIds;
    }

    public JsonOverrideProvider(
            String providerId, MapAttributesImpl attributes, Set<String> featureIds, Set<String> categoryIds) {
        this(1, providerId, attributes, featureIds, categoryIds);
    }

    public String getProviderId() {
        return "json-override:" + providerId;
    }

    @Override
    public MapAttributes getOverrideAttributes(MapFeature mapFeature) {
        return attributes;
    }

    @Override
    public Stream<String> getOverridenFeatureIds() {
        return featureIds.stream();
    }

    @Override
    public Stream<String> getOverridenCategoryIds() {
        return categoryIds.stream();
    }

    @Override
    public void onChange(Consumer<MapDataProvidedType> callback) {
        // no-op at the moment, json override providers are "final" classes that are recreated on change
    }

    @Override
    public int compareTo(JsonOverrideProvider jsonOverrideProvider) {
        return this.providerId.compareTo(jsonOverrideProvider.providerId);
    }
}

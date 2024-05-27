/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This is an implementation of MapAttributes that are guarenteed to never return
 * Optional.empty() for any value. It does this by extending the lookup for the
 * attribute first to the category hierarchy for the given feature, and
 * finally by going to the default value for each attribute.
 */
public class ResolvedMapAttributes extends DerivedMapAttributes {
    private final MapFeature feature;
    private final MapAttributes attributes;

    public ResolvedMapAttributes(MapFeature feature) {
        this.feature = feature;
        this.attributes = feature.getAttributes().orElse(null);
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return Optional.of(new ResolvedMapVisibility(this, MapAttributes::getLabelVisibility));
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return Optional.of(new ResolvedMapVisibility(this, MapAttributes::getIconVisibility));
    }

    @Override
    protected <T> Optional<T> getAttribute(Function<MapAttributes, Optional<T>> getter) {
        // Check if the feature has overridden this attribute
        Optional<T> featureAttribute = getFromFeature(getter);
        if (featureAttribute.isPresent()) {
            return featureAttribute;
        }

        // Then try to get it from the category
        for (String id = getCategoryId(); id != null; id = getParentCategoryId(id)) {
            Stream<T> attributes = getAttributesForCategoryId(getter, id);

            Optional<T> attribute = attributes.findFirst();
            if (attribute.isPresent()) {
                return attribute;
            }
        }

        // Otherwise return the fallback default value
        return getter.apply(DefaultMapAttributes.INSTANCE);
    }

    String getCategoryId() {
        return feature.getCategoryId();
    }

    <T> Optional<T> getFromFeature(Function<MapAttributes, Optional<T>> attributeGetter) {
        if (attributes != null) {
            Optional<T> attribute = attributeGetter.apply(attributes);
            return attribute;
        }

        return Optional.empty();
    }

    static <T> Stream<T> getAttributesForCategoryId(
            Function<MapAttributes, Optional<T>> attributeGetter, String categoryId) {
        // Find all provided MapAttributes for this category level
        Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(categoryId)
                .map(MapCategory::getAttributes)
                .filter(Optional::isPresent)
                .map(Optional::get);

        // Mulitple providers might provide MapAttributes to the same category, but not
        // all of them might provide the attribute we're actually looking for, so
        // check all (in the arbitrary order that Services.MapData gave them to us).
        return allAttributes.map(attributeGetter).filter(Optional::isPresent).map(Optional::get);
    }

    static String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }
}

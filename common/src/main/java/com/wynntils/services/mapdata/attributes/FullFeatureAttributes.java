/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.core.components.Services;
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
public class FullFeatureAttributes extends DerivedAttributes {
    private final MapFeature feature;
    private final MapAttributes attributes;

    public FullFeatureAttributes(MapFeature feature) {
        this.feature = feature;
        this.attributes = feature.getAttributes().orElse(null);
    }

    @Override
    protected <T> Optional<T> getAttribute(Function<MapAttributes, Optional<T>> getter) {
        // Check if the feature has overridden this attribute
        Optional<T> featureAttribute = getFromFeature(getter);
        if (featureAttribute.isPresent()) {
            return featureAttribute;
        }

        // Then try to get it from the category
        Optional<T> categoryAttribute = getFromCategory(getter);
        if (categoryAttribute.isPresent()) {
            return categoryAttribute;
        }

        // Otherwise return the fallback default value
        return getter.apply(DefaultAttributes.INSTANCE);
    }

    private <T> Optional<T> getFromFeature(Function<MapAttributes, Optional<T>> attributeGetter) {
        if (attributes != null) {
            Optional<T> attribute = attributeGetter.apply(attributes);
            return attribute;
        }

        return Optional.empty();
    }

    private Optional<Float> getVisibilityFeatureValue(
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter,
            Function<MapVisibility, Optional<Float>> valueGetter) {
        if (attributes != null) {
            Optional<MapVisibility> featureVisibility = attributeGetter.apply(attributes);
            if (featureVisibility.isPresent()) {
                return valueGetter.apply(featureVisibility.get());
            }
        }
        return Optional.empty();
    }

    protected <T> Optional<T> getFromCategory(Function<MapAttributes, Optional<T>> attributeGetter) {
        for (String id = feature.getCategoryId(); id != null; id = getParentCategoryId(id)) {
            // Find all provided MapAttributes for this category level
            Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(id)
                    .map(MapCategory::getAttributes)
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            // Mulitple providers might provide MapAttributes to the same category, but not
            // all of them might provide the attribute we're actually looking for, so
            // check all (in the arbitrary order that Services.MapData gave them to us).
            Optional<T> attribute = allAttributes
                    .map(attributeGetter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (attribute.isPresent()) {
                return attribute;
            }
        }

        return Optional.empty();
    }

    private Optional<Float> getVisibilityFromCategory(
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter,
            Function<MapVisibility, Optional<Float>> valueGetter) {
        for (String id = feature.getCategoryId(); id != null; id = getParentCategoryId(id)) {
            // Find all provided MapAttributes for this category level
            Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(id)
                    .map(MapCategory::getAttributes)
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            // Mulitple providers might provide MapAttributes to the same category, but not
            // all of them might provide the attribute we're actually looking for, so
            // check all (in the arbitrary order that Services.MapData gave them to us).
            Stream<MapVisibility> attributes = allAttributes
                    .map(attributeGetter)
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            // And finally check each visibility in turn for the actual value we have
            // been asked to provide
            Optional<Float> attribute = attributes
                    .map(valueGetter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (attribute.isPresent()) {
                return attribute;
            }
        }

        return Optional.empty();
    }

    private Optional<Float> getVisibilityAttribute(
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter,
            Function<MapVisibility, Optional<Float>> getter) {
        // Check if the feature has overridden this attribute
        Optional<Float> featureValue = getVisibilityFeatureValue(attributeGetter, getter);
        if (featureValue.isPresent()) {
            return featureValue;
        }

        // Otherwise try to get it from the category
        Optional<Float> categoryValue = getVisibilityFromCategory(attributeGetter, getter);
        if (categoryValue.isPresent()) {
            return categoryValue;
        }

        // Otherwise return default
        return getter.apply(attributeGetter.apply(DefaultAttributes.INSTANCE).get());
    }

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return Optional.of(new FullMapVisibility(MapAttributes::getLabelVisibility));
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return Optional.of(new FullMapVisibility(MapAttributes::getIconVisibility));
    }

    public class FullMapVisibility extends DerivedMapVisibility {
        private final Function<MapAttributes, Optional<MapVisibility>> attributeGetter;

        public FullMapVisibility(Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
            this.attributeGetter = attributeGetter;
        }

        @Override
        protected Optional<Float> getVisibilityFor(Function<MapVisibility, Optional<Float>> getter) {
            return getVisibilityAttribute(attributeGetter, getter);
        }
    }
}

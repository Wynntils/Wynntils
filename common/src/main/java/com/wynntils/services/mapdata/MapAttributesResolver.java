/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.attributes.type.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.type.ResolvedMapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This is an implementation of MapAttributes that are guarenteed to never return
 * Optional.empty() for any value. It does this by extending the lookup for the
 * attribute first to the category hierarchy for the given feature, and
 * finally by going to the default value for each attribute.
 */
public class MapAttributesResolver {
    private final MapFeature feature;

    private MapAttributesResolver(MapFeature feature) {
        this.feature = feature;
    }

    public static ResolvedMapAttributes resolve(MapFeature feature) {
        MapAttributesResolver resolver = new MapAttributesResolver(feature);
        return new ResolvedMapAttributes(
                resolver.getLabel(),
                resolver.getIconId(),
                resolver.getPriority(),
                resolver.getLevel(),
                resolver.getLabelVisibility(),
                resolver.getLabelColor(),
                resolver.getLabelShadow(),
                resolver.getIconVisibility(),
                resolver.getIconColor(),
                resolver.getIconDecoration());
    }

    private String getLabel() {
        return getAttribute(MapAttributes::getLabel);
    }

    private String getIconId() {
        return getAttribute(MapAttributes::getIconId);
    }

    private int getPriority() {
        return getAttribute(MapAttributes::getPriority);
    }

    private int getLevel() {
        return getAttribute(MapAttributes::getLevel);
    }

    private CustomColor getLabelColor() {
        return getAttribute(MapAttributes::getLabelColor);
    }

    private TextShadow getLabelShadow() {
        return getAttribute(MapAttributes::getLabelShadow);
    }

    private CustomColor getIconColor() {
        return getAttribute(MapAttributes::getIconColor);
    }

    private MapDecoration getIconDecoration() {
        return getAttribute(MapAttributes::getIconDecoration);
    }

    private ResolvedMapVisibility getLabelVisibility() {
        return getResolvedMapVisibility(MapAttributes::getLabelVisibility);
    }

    private ResolvedMapVisibility getIconVisibility() {
        return getResolvedMapVisibility(MapAttributes::getIconVisibility);
    }

    private ResolvedMapVisibility getResolvedMapVisibility(
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        return new ResolvedMapVisibility(getMin(attributeGetter), getMax(attributeGetter), getFade(attributeGetter));
    }

    private float getMin(Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        return getVisibilityValue(MapVisibility::getMin, attributeGetter);
    }

    private float getMax(Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        return getVisibilityValue(MapVisibility::getMax, attributeGetter);
    }

    private float getFade(Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        return getVisibilityValue(MapVisibility::getFade, attributeGetter);
    }

    protected <T> T getAttribute(Function<MapAttributes, Optional<T>> getter) {
        // Check if the feature has overridden this attribute
        Optional<T> featureAttribute = getFromFeature(getter);
        if (featureAttribute.isPresent()) {
            return featureAttribute.get();
        }

        // Then try to get it from the category
        for (String id = getCategoryId(); id != null; id = getParentCategoryId(id)) {
            Stream<T> attributes = getAttributesForCategoryId(getter, id);

            Optional<T> attribute = attributes.findFirst();
            if (attribute.isPresent()) {
                return attribute.get();
            }
        }

        // Otherwise return the fallback default value
        return getter.apply(DefaultMapAttributes.INSTANCE).get();
    }

    private float getVisibilityValue(
            Function<MapVisibility, Optional<Float>> getter,
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        // Check if the feature has overridden this attribute
        Optional<MapVisibility> featureVisibility = getFromFeature(attributeGetter);
        if (featureVisibility.isPresent()) {
            // We got the attribute, but do we got the value?
            Optional<Float> featureValue = getter.apply(featureVisibility.get());
            if (featureValue.isPresent()) {
                return featureValue.get();
            }
        }

        // Otherwise try to get it from the category
        for (String id = getCategoryId(); id != null; id = getParentCategoryId(id)) {
            Stream<MapVisibility> attributes = getAttributesForCategoryId(attributeGetter, id);

            // Then check each visibility in turn for the value we're looking for
            Optional<Float> attribute = attributes
                    .map(getter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (attribute.isPresent()) {
                return attribute.get();
            }
        }

        // Otherwise return the fallback default value
        return getter.apply(attributeGetter.apply(DefaultMapAttributes.INSTANCE).get())
                .get();
    }

    private String getCategoryId() {
        return feature.getCategoryId();
    }

    private <T> Optional<T> getFromFeature(Function<MapAttributes, Optional<T>> attributeGetter) {
        Optional<MapAttributes> attributes = feature.getAttributes();
        if (attributes.isEmpty()) return Optional.empty();

        return attributeGetter.apply(attributes.get());
    }

    private <T> Stream<T> getAttributesForCategoryId(
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

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }
}

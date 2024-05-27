/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.services.mapdata.attributes.DefaultAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ResolvedMapVisibility extends DerivedMapVisibility {
    private final ResolvedMapAttributes resolvedMapAttributes;
    private final Function<MapAttributes, Optional<MapVisibility>> attributeGetter;

    public ResolvedMapVisibility(
            ResolvedMapAttributes resolvedMapAttributes,
            Function<MapAttributes, Optional<MapVisibility>> attributeGetter) {
        this.resolvedMapAttributes = resolvedMapAttributes;
        this.attributeGetter = attributeGetter;
    }

    @Override
    protected Optional<Float> getValue(Function<MapVisibility, Optional<Float>> getter) {
        // Check if the feature has overridden this attribute
        Optional<MapVisibility> featureVisibility = resolvedMapAttributes.getFromFeature(attributeGetter);
        if (featureVisibility.isPresent()) {
            Optional<Float> featureValue = getter.apply(featureVisibility.get());
            if (featureValue.isPresent()) {
                return featureValue;
            }
        }

        // Otherwise try to get it from the category
        for (String id = resolvedMapAttributes.getCategoryId();
                id != null;
                id = resolvedMapAttributes.getParentCategoryId(id)) {
            Stream<MapVisibility> attributes = resolvedMapAttributes.getAttributesForCategoryId(attributeGetter, id);

            // Then check each visibility in turn for the value we're looking for
            Optional<Float> attribute = attributes
                    .map(getter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (attribute.isPresent()) {
                return attribute;
            }
        }

        // Otherwise return the fallback default value
        return getter.apply(attributeGetter.apply(DefaultAttributes.INSTANCE).get());
    }
}

/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.attributes.type.DerivedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.FullMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class FullCategoryAttributes extends DerivedAttributes {
    private final String categoryId;

    public FullCategoryAttributes(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    protected <T> Optional<T> getAttribute(Function<MapAttributes, Optional<T>> getter) {
        for (String id = categoryId; id != null; id = getParentCategoryId(id)) {
            Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(id)
                    .map(MapCategory::getAttributes)
                    .filter(Objects::nonNull);
            Optional<Optional<T>> possibleAttribute =
                    allAttributes.map(getter).filter(Optional::isPresent).findFirst();
            if (possibleAttribute.isPresent()) {
                return possibleAttribute.get();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return Optional.of(getVisibilityAttribute(MapAttributes::getLabelVisibility));
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return Optional.of(getVisibilityAttribute(MapAttributes::getIconVisibility));
    }

    private <T extends MapVisibility> FullMapVisibility getVisibilityAttribute(
            Function<MapAttributes, Optional<T>> getter) {
        Deque<DerivedMapVisibility> visibilityStack = new LinkedList<>();

        for (String id = categoryId; id != null; id = getParentCategoryId(id)) {
            Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(id)
                    .map(MapCategory::getAttributes)
                    .filter(Objects::nonNull);
            Optional<T> visibility = allAttributes
                    .map(getter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            if (visibility.isPresent()) {
                if (visibility.get() instanceof FullMapVisibility fullVisibility) {
                    // Apply the visibility stack to the full visibility
                    FullMapVisibility result = fullVisibility;
                    while (!visibilityStack.isEmpty()) {
                        // We want to apply the highest category's visibility first
                        result = result.applyDerived(visibilityStack.pollLast());
                    }
                    return result;
                } else if (visibility.get() instanceof DerivedMapVisibility derivedVisibility) {
                    visibilityStack.push(derivedVisibility);
                } else {
                    WynntilsMod.warn(
                            "Unhandled visibility type #1: " + visibility.get().getClass());
                    return MapVisibility.ALWAYS;
                }
            }
        }

        FullMapVisibility result = MapVisibility.ALWAYS;

        if (!visibilityStack.isEmpty()) {
            for (DerivedMapVisibility derivedVisibility : visibilityStack) {
                result = result.applyDerived(derivedVisibility);
            }
            return result;
        }

        return result;
    }

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }
}

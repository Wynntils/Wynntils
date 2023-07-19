/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.WaypointProvider;
import com.wynntils.models.mapdata.style.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.MapFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    public static final String ROOT_CATEGORY_NAME = "ROOT CATEGORY";
    private final List<MapDataProvider> providers = List.of(new WaypointProvider());
    private Map<String, String> categoryDisplayNames = new HashMap<>();
    private Map<String, MapFeatureAttributes> categoryStyles = new HashMap<>();

    public MapDataModel() {
        super(List.of());

    public void updateCategory(String categoryId, String displayName, MapFeatureAttributes attributes) {
        if (displayName != null) {
            categoryDisplayNames.put(categoryId, displayName);
        }
        if (attributes != null) {
            categoryStyles.put(categoryId, attributes);
        }
    }

    public String getDisplayName(MapFeature feature) {
        return null;
    }

    private MapFeatureAttributes getParentStyle(String categoryId) {
        String parentCategoryId = getParentCategoryId(categoryId);
        if (parentCategoryId == null) return MapFeatureAttributes.EMPTY;

        return getCategoryStyle(parentCategoryId);
    }

    private MapFeatureAttributes getCategoryStyle(String categoryId) {
        if (categoryId == null) return MapFeatureAttributes.EMPTY;

        MapFeatureAttributes style = categoryStyles.get(categoryId);
        if (style != null) return style;

        // Recursively check the parent
        return getCategoryStyle(getParentCategoryId(categoryId));
    }

    public String getCategoryName(String categoryId) {
        if (categoryId == null) return ROOT_CATEGORY_NAME;

        String name = categoryDisplayNames.get(categoryId);
        if (name != null) return name;

        // Recursively check the parent
        return getCategoryName(getParentCategoryId(categoryId));
    }

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }

    public Stream<MapFeature> getFeatures() {
        Stream<MapFeature> allFeatures = providers.stream().flatMap(MapDataProvider::getFeatures);
        return allFeatures;
    }
}

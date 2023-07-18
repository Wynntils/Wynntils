/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.WaypointProvider;
import com.wynntils.models.mapdata.style.MapFeatureStyle;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.MapFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    private final List<MapDataProvider> providers = List.of(new WaypointProvider());
    private List<MapCategory> categories = new ArrayList();
    private Map<String, String> categoryDisplayNames = new HashMap<>();
    private Map<String, MapFeatureStyle> categoryStyles = new HashMap<>();

    public MapDataModel() {
        super(List.of());


    }

    public void updateCategory(String categoryId, String displayName, MapFeatureStyle style) {
        MapCategory category = getCategoryFromId(categoryId);
        if (category != null) {
            return;
        }
        if (categories.contains(category)) return;

        MapCategory parent = getOrAdd(category.parent());

    }

    private MapCategory getCategoryFromId(String categoryId) {
    }

    private MapCategory getOrAdd(MapCategory parent) {
    }

    public Stream<MapFeature> getFeatures() {
        Stream<MapFeature> allFeatures = providers.stream().flatMap(MapDataProvider::getFeatures);
        return allFeatures;
    }

    public List<MapCategory> getCategories() {
        return categories;
    }
}

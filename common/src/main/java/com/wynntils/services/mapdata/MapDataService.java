/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Service;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.mapdata.attributes.FullCategoryAttributes;
import com.wynntils.services.mapdata.attributes.FullFeatureAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.providers.MapDataProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MapDataService extends Service {
    private final MapDataProviders providers = new MapDataProviders();

    public MapDataService() {
        super(List.of());
    }

    public Stream<MapFeature> getFeatures() {
        return providers.getProviders().flatMap(MapDataProvider::getFeatures);
    }

    public Stream<Poi> getFeaturesAsPois() {
        return getFeatures().map(MapFeaturePoiWrapper::new);
    }

    public MapAttributes getFullFeatureAttributes(MapFeature feature) {
        return new FullFeatureAttributes(feature);
    }

    public String getCategoryName(String categoryId) {
        return getCategoryDefinitions(categoryId)
                .map(MapCategory::getName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("NAMELESS CATEGORY");
    }

    public MapIcon getIcon(String iconId) {
        if (iconId.equals(MapIcon.NO_ICON_ID)) {
            // should return null but we cant handle that
            // FIXME
            Stream<MapIcon> allIcons = providers.getProviders().flatMap(MapDataProvider::getIcons);
            MapIcon icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:symbols:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }

        Stream<MapIcon> allIcons = providers.getProviders().flatMap(MapDataProvider::getIcons);
        MapIcon icon =
                allIcons.filter(i -> i.getIconId().equals(iconId)).findFirst().orElse(null);

        if (icon == null) {
            allIcons = providers.getProviders().flatMap(MapDataProvider::getIcons);
            icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:symbols:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }
        return icon;
    }

    // region Support for FullFeatureAttributes and FullCategoryAttributes

    public MapAttributes getFullCategoryAttributes(String categoryId) {
        return new FullCategoryAttributes(categoryId);
    }

    public Stream<MapCategory> getCategoryDefinitions(String categoryId) {
        return providers.getProviders().flatMap(MapDataProvider::getCategories).filter(p -> p.getCategoryId()
                .equals(categoryId));
    }

    // endregion
}

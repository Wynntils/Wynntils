/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.attributes.FullCategoryAttributes;
import com.wynntils.models.mapdata.attributes.FullFeatureAttributes;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapIcon;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.services.map.pois.Poi;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    private final MapDataProviders providers = new MapDataProviders();

    public MapDataModel() {
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
        return getCategories(categoryId)
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

    public MapAttributes getFullCategoryAttributes(String categoryId) {
        return new FullCategoryAttributes(categoryId);
    }

    public Stream<MapCategory> getCategories(String categoryId) {
        return providers.getProviders().flatMap(MapDataProvider::getCategories).filter(p -> p.getCategoryId()
                .equals(categoryId));
    }
}

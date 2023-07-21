/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.attributes.MapIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.services.map.pois.Poi;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    MapDataProviders providers = new MapDataProviders();

    public MapDataModel() {
        super(List.of());
    }

    public MapAttributes getInheritedAttributes(MapFeature feature) {
        return new MapInheritedAttributes(feature);
    }

    public MapAttributes getCategoryAttributes(String categoryId) {
        Stream<MapCategory> allCategories = providers.getProviders().flatMap(MapDataProvider::getCategories);
        MapAttributes attributes = allCategories
                .filter(c -> c.getCategoryId().equals(categoryId))
                .map(MapCategory::getAttributes)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return attributes;
    }

    public String getCategoryName(String categoryId) {
        Stream<MapCategory> allCategories = providers.getProviders().flatMap(MapDataProvider::getCategories);
        String displayName = allCategories
                .filter(c -> c.getCategoryId().equals(categoryId))
                .map(MapCategory::getName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("NAMELESS CATEGORY");
        return displayName;
    }

    public Stream<MapFeature> getFeatures() {
        Stream<MapFeature> allFeatures = providers.getProviders().flatMap(MapDataProvider::getFeatures);
        return allFeatures;
    }

    public Stream<Poi> getFeaturesAsPois() {
        return getFeatures().map(MapFeaturePoiWrapper::new);
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
}

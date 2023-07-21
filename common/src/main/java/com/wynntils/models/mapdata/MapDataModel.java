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
import java.util.function.Function;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    MapDataProviders providers = new MapDataProviders();

    public MapDataModel() {
        super(List.of());
    }

    public MapAttributes getAttributes(MapFeature feature) {
        return new MapInheritedAttributes(feature);
    }

    public <T> T getFeatureAttribute(MapFeature feature, Function<MapAttributes, T> getter) {
        MapAttributes attributes = feature.getAttributes();
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null) {
                return attribute;
            }
        }

        return getCategoryAttribute(feature.getCategoryId(), getter);
    }

    private <T> T getCategoryAttribute(String categoryId, Function<MapAttributes, T> getter) {
        if (categoryId == null) {
            // FIXME: proper detection for root, proper root style
            return null;
        }

        MapAttributes attributes = getAttributeForCategoryId(categoryId);
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null) {
                return attribute;
            }
        }

        String parentId = getParentCategoryId(categoryId);
        return getCategoryAttribute(parentId, getter);
    }

    private MapAttributes getAttributeForCategoryId(String categoryId) {
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

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
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
            MapIcon icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }

        Stream<MapIcon> allIcons = providers.getProviders().flatMap(MapDataProvider::getIcons);
        MapIcon icon =
                allIcons.filter(i -> i.getIconId().equals(iconId)).findFirst().orElse(null);

        if (icon == null) {
            allIcons = providers.getProviders().flatMap(MapDataProvider::getIcons);
            icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }
        return icon;
    }
}

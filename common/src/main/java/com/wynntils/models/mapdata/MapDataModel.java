/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.LocalProvider;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.CategoriesProvider;
import com.wynntils.models.mapdata.providers.builtin.CharacterProvider;
import com.wynntils.models.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.services.map.pois.Poi;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    private final List<MapDataProvider> providers =
            List.of(new CategoriesProvider(), new CharacterProvider(), new MapIconsProvider(), new LocalProvider());

    public MapDataModel() {
        super(List.of());
    }

    public MapFeatureAttributes getAttributes(MapFeature feature) {
        return new MapFeatureInheritedAttributes(feature);
    }

    public <T> T getFeatureAttribute(MapFeature feature, Function<MapFeatureAttributes, T> getter) {
        MapFeatureAttributes attributes = feature.getAttributes();
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null) {
                return attribute;
            }
        }

        return getCategoryAttribute(feature.getCategoryId(), getter);
    }

    private <T> T getCategoryAttribute(String categoryId, Function<MapFeatureAttributes, T> getter) {
        if (categoryId == null) {
            // FIXME: proper detection for root, proper root style
            return null;
        }

        MapFeatureAttributes attributes = getAttributeForCategoryId(categoryId);
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null) {
                return attribute;
            }
        }

        String parentId = getParentCategoryId(categoryId);
        return getCategoryAttribute(parentId, getter);
    }

    private MapFeatureAttributes getAttributeForCategoryId(String categoryId) {
        Stream<MapFeatureCategory> allCategories = providers.stream().flatMap(MapDataProvider::getCategories);
        MapFeatureAttributes attributes = allCategories
                .filter(c -> c.getCategoryId().equals(categoryId))
                .map(MapFeatureCategory::getAttributes)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return attributes;
    }

    public String getCategoryName(String categoryId) {
        Stream<MapFeatureCategory> allCategories = providers.stream().flatMap(MapDataProvider::getCategories);
        String displayName = allCategories
                .filter(c -> c.getCategoryId().equals(categoryId))
                .map(MapFeatureCategory::getDisplayName)
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
        Stream<MapFeature> allFeatures = providers.stream().flatMap(MapDataProvider::getFeatures);
        return allFeatures;
    }

    public Stream<Poi> getFeaturesAsPois() {
        return getFeatures().map(FeaturePoi::new);
    }

    public MapFeatureIcon getIcon(String iconId) {
        if (iconId.equals(MapFeatureIcon.NO_ICON_ID)) {
            // should return null but we cant handle that
            // FIXME
            Stream<MapFeatureIcon> allIcons = providers.stream().flatMap(MapDataProvider::getIcons);
            MapFeatureIcon icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }

        Stream<MapFeatureIcon> allIcons = providers.stream().flatMap(MapDataProvider::getIcons);
        MapFeatureIcon icon =
                allIcons.filter(i -> i.getIconId().equals(iconId)).findFirst().orElse(null);

        if (icon == null) {
            allIcons = providers.stream().flatMap(MapDataProvider::getIcons);
            icon = allIcons.filter(i -> i.getIconId().equals("wynntils:icon:waypoint"))
                    .findFirst()
                    .orElse(null);
            return icon;
        }
        return icon;
    }
}

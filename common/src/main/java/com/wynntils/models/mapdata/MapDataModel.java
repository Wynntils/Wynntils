/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.CharacterProvider;
import com.wynntils.models.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.utils.render.Texture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    public static final String ROOT_CATEGORY_NAME = "ROOT CATEGORY";
    private final List<MapDataProvider> providers = List.of(new CharacterProvider(), new MapIconsProvider());
    private Map<String, String> categoryDisplayNames = new HashMap<>();
    private Map<String, MapFeatureAttributes> categoryAttributes = new HashMap<>();
    private Map<String, Texture> iconTextures = new HashMap<>();

    public MapDataModel() {
        super(List.of());
    }

    public void updateCategory(String categoryId, String displayName, MapFeatureAttributes attributes) {
        if (displayName != null) {
            categoryDisplayNames.put(categoryId, displayName);
        }
        if (attributes != null) {
            categoryAttributes.put(categoryId, attributes);
        }
    }

    public void updateIcon(String iconId, Texture texture) {
        if (texture != null) {
            iconTextures.put(iconId, texture);
        }
    }

    public MapFeatureAttributes getAttributes(MapFeature feature) {
        return new MapFeatureConcreteAttributes(feature);
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

    public <T> T getCategoryAttribute(String categoryId, Function<MapFeatureAttributes, T> getter) {
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
        return categoryAttributes.get(categoryId);
    }

    public String getCategoryName(String categoryId) {
        if (categoryId == null) return ROOT_CATEGORY_NAME;

        String name = categoryDisplayNames.get(categoryId);
        if (name != null) return name;

        return "NAMELESS CATEGORY";
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

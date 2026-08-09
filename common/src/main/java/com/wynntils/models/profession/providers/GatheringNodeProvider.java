/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.providers;

import com.wynntils.core.components.Models;
import com.wynntils.models.profession.type.GatheringNodeType;
import com.wynntils.models.profession.type.MaterialType;
import com.wynntils.models.profession.type.SourceMaterial;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the gathering nodes downloaded by {@link com.wynntils.models.profession.GatheringNodeRegistry}.
 * <p>
 * The per-resource categories are not hardcoded; they are built from the categories present in the
 * downloaded data and resolved against the material registry, so a resource Wynncraft adds later
 * needs no code change here.
 */
public class GatheringNodeProvider extends BuiltInProvider {
    public static final String GATHERING_CATEGORY_ID = "wynntils:gathering";
    public static final String UNKNOWN_GATHERING_CATEGORY_ID = GATHERING_CATEGORY_ID + ":unknown";

    private static final List<MapFeature> ALL_FEATURES = new ArrayList<>();
    private static final List<MapFeature> VISIBLE_FEATURES = new ArrayList<>();
    private static final List<MapCategory> RESOURCE_CATEGORIES = new ArrayList<>();
    private static final Map<String, GatheringNodeType> NODE_TYPES = new HashMap<>();

    @Override
    public String getProviderId() {
        return "gathering-nodes";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return VISIBLE_FEATURES.stream();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return RESOURCE_CATEGORIES.stream();
    }

    @Override
    public void reloadData() {
        // The data is downloaded by GatheringNodeRegistry, which needs the material registry to be
        // downloaded first, a dependency that can only be expressed through the download registry.
    }

    public void updateNodes(List<GatheringNodeLocation> nodes) {
        ALL_FEATURES.forEach(this::notifyCallbacks);
        ALL_FEATURES.clear();
        ALL_FEATURES.addAll(nodes);

        rebuildNodeTypes();
        applyFilter();
    }

    public List<GatheringNodeType> getNodeTypes() {
        return NODE_TYPES.values().stream().sorted().toList();
    }

    /**
     * Recomputes which nodes are shown on the map. Nodes of a resource that could not be resolved
     * against the material registry are always shown, as they cannot be filtered.
     */
    public void applyFilter() {
        // No callbacks here on purpose. Only the set of shown features changes, not the attributes
        // of any of them, so there is nothing cached to invalidate.
        VISIBLE_FEATURES.clear();

        for (MapFeature feature : ALL_FEATURES) {
            GatheringNodeType nodeType = NODE_TYPES.get(feature.getCategoryId());
            if (nodeType == null || Models.Profession.isGatheringNodeTypeVisible(nodeType)) {
                VISIBLE_FEATURES.add(feature);
            }
        }
    }

    private void rebuildNodeTypes() {
        RESOURCE_CATEGORIES.forEach(this::notifyCallbacks);
        RESOURCE_CATEGORIES.clear();
        NODE_TYPES.clear();

        for (String categoryId :
                ALL_FEATURES.stream().map(MapFeature::getCategoryId).distinct().toList()) {
            Optional<Pair<MaterialType, SourceMaterial>> material =
                    Models.Profession.findMaterialBySourceName(getResourceName(categoryId));
            if (material.isEmpty()) continue;

            GatheringNodeType nodeType = new GatheringNodeType(
                    categoryId, material.get().key(), material.get().value());

            NODE_TYPES.put(categoryId, nodeType);
            RESOURCE_CATEGORIES.add(new GatheringResourceCategory(nodeType));
        }
    }

    public static String getProfessionCategoryId(MaterialType materialType) {
        return GATHERING_CATEGORY_ID + ":"
                + materialType.getProfessionType().name().toLowerCase(Locale.ROOT);
    }

    /**
     * Turns the resource segment of a category id back into a source material name,
     * e.g. {@code wynntils:gathering:woodcutting:red-alder} into {@code Red Alder}.
     */
    private static String getResourceName(String categoryId) {
        String slug = categoryId.substring(categoryId.lastIndexOf(':') + 1);

        return Arrays.stream(slug.split("-")).map(StringUtils::capitalized).collect(Collectors.joining(" "));
    }

    public static final class GatheringNodeLocation extends MapLocationImpl {
        public GatheringNodeLocation(
                String featureId, String categoryId, MapLocationAttributesImpl attributes, Location location) {
            super(featureId, categoryId, attributes, location);
        }
    }

    private static final class GatheringResourceCategory implements MapCategory {
        private final GatheringNodeType nodeType;

        private GatheringResourceCategory(GatheringNodeType nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public String getCategoryId() {
            return nodeType.categoryId();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(nodeType.getDisplayName());
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(nodeType.getDisplayName());
                }

                @Override
                public Optional<Integer> getLevel() {
                    return Optional.of(nodeType.sourceMaterial().level());
                }
            });
        }
    }
}

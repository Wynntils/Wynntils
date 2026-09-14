/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.providers;

import com.wynntils.core.WynntilsMod;
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
 * <p>
 * The download handler runs off the render thread, while the filter is applied from the screen on
 * the render thread, so all state is published by swapping an immutable snapshot rather than by
 * mutating a shared collection.
 */
public class GatheringNodeProvider extends BuiltInProvider {
    public static final String GATHERING_CATEGORY_ID = "wynntils:gathering";

    private volatile List<MapFeature> allFeatures = List.of();
    private volatile List<MapFeature> visibleFeatures = List.of();
    private volatile List<MapCategory> resourceCategories = List.of();
    private volatile Map<String, GatheringNodeType> nodeTypes = Map.of();

    @Override
    public String getProviderId() {
        return "gathering-nodes";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return visibleFeatures.stream();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return resourceCategories.stream();
    }

    @Override
    public void reloadData() {
        // The data is downloaded by GatheringNodeRegistry, which needs the material registry to be
        // downloaded first, a dependency that can only be expressed through the download registry.
    }

    public void updateNodes(List<GatheringNodeLocation> nodes) {
        allFeatures.forEach(this::notifyCallbacks);
        allFeatures = List.copyOf(nodes);

        rebuildNodeTypes();
        applyFilter();
    }

    public List<GatheringNodeType> getNodeTypes() {
        return nodeTypes.values().stream().sorted().toList();
    }

    /**
     * Recomputes which nodes are shown on the map. No callbacks are fired, as only the set of shown
     * features changes, not the attributes of any of them, so there is nothing cached to invalidate.
     */
    public void applyFilter() {
        Map<String, GatheringNodeType> currentNodeTypes = nodeTypes;

        visibleFeatures = allFeatures.stream()
                .filter(feature -> {
                    GatheringNodeType nodeType = currentNodeTypes.get(feature.getCategoryId());
                    return nodeType != null && Models.Profession.isGatheringNodeTypeVisible(nodeType);
                })
                .toList();
    }

    private void rebuildNodeTypes() {
        resourceCategories.forEach(this::notifyCallbacks);

        Map<String, GatheringNodeType> newNodeTypes = new HashMap<>();
        List<MapCategory> newCategories = new ArrayList<>();
        List<String> unresolved = new ArrayList<>();

        for (String categoryId :
                allFeatures.stream().map(MapFeature::getCategoryId).distinct().toList()) {
            Optional<Pair<MaterialType, SourceMaterial>> material =
                    Models.Profession.findMaterialBySourceName(getResourceName(categoryId));
            if (material.isEmpty()) {
                unresolved.add(categoryId);
                continue;
            }

            GatheringNodeType nodeType = new GatheringNodeType(
                    categoryId, material.get().key(), material.get().value());

            newNodeTypes.put(categoryId, nodeType);
            newCategories.add(new GatheringResourceCategory(nodeType));
        }

        if (!unresolved.isEmpty()) {
            // Nodes of these categories cannot be named or filtered, so they are not shown at all
            WynntilsMod.warn("Unknown gathering node resources, they will not be shown on the map: " + unresolved);
        }

        nodeTypes = Map.copyOf(newNodeTypes);
        resourceCategories = List.copyOf(newCategories);
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

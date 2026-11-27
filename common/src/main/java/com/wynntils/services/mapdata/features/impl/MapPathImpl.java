/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.impl;

import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.utils.type.PathGeometry;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;

/**
 * Implementation of {@link MapPath}. Extend this class, if a serializable subclass is desired.
 */
public class MapPathImpl implements MapPath {
    private final String featureId;
    private final String categoryId;
    private final MapPathAttributesImpl attributes;
    private final List<Vec3> path;
    private final List<Vec3> highlights;
    private final List<PathNote> notes;
    private transient List<Vec3> mapPath;

    public MapPathImpl(String featureId, String categoryId, MapPathAttributesImpl attributes, List<Vec3> path) {
        this(featureId, categoryId, attributes, path, List.of(), List.of());
    }

    public MapPathImpl(
            String featureId,
            String categoryId,
            MapPathAttributesImpl attributes,
            List<Vec3> path,
            List<Vec3> highlights,
            List<PathNote> notes) {
        this.featureId = featureId;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.path = List.copyOf(path);
        this.highlights = List.copyOf(highlights);
        this.notes = List.copyOf(notes);
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public Optional<MapPathAttributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public List<Vec3> getPath() {
        return path;
    }

    @Override
    public List<Vec3> getMapPath() {
        if (mapPath == null) {
            mapPath = PathGeometry.simplifyForMap(path);
        }

        return mapPath;
    }

    @Override
    public List<Vec3> getHighlights() {
        return highlights == null ? List.of() : highlights;
    }

    @Override
    public List<PathNote> getNotes() {
        return notes == null ? List.of() : notes;
    }

    public boolean validate() {
        return featureId != null
                && categoryId != null
                && path != null
                && path.size() >= 2
                && path.stream()
                        .allMatch(p -> p != null
                                && (Double.isFinite(p.x()) && Double.isFinite(p.y()) && Double.isFinite(p.z())))
                && getHighlights().stream()
                        .allMatch(p -> p != null
                                && (Double.isFinite(p.x()) && Double.isFinite(p.y()) && Double.isFinite(p.z())))
                && getNotes().stream().allMatch(n -> n != null && n.validate());
    }
}

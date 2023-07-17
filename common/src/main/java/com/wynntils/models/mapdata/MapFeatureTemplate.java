package com.wynntils.models.mapdata;

public class MapFeatureTemplate {
    private final MapFeatureTemplate parent;

    public MapFeatureTemplate(MapFeatureTemplate parent) {
        this.parent = parent;
    }

    public MapFeatureTemplate getParent() {
        return parent;
    }
}

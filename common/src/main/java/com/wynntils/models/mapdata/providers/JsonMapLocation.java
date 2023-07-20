package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.models.mapdata.type.features.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.List;

public class JsonMapLocation implements MapLocation {
    private final String id;
    private final String category;
    private final Location location;

    public JsonMapLocation(String id, String category, Location location) {
        this.id = id;
        this.category = category;
        this.location = location;
    }

    @Override
    public String getFeatureId() {
        return id;
    }

    @Override
    public String getCategoryId() {
        return category;
    }

    @Override
    public MapFeatureAttributes getAttributes() {
        return null;
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public Location getLocation() {
        return location;
    }
}

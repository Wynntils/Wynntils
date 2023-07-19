/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type;

import com.wynntils.models.mapdata.style.MapFeatureAttributes;
import java.util.Objects;

// wynntils:lootrun:chest:tier1
// wynntils:service:profession:scribing
// wynntils:service:identifier
// wynntils:npc:quest
// wynntils:personal:lootrunpath
// wynntils:personal:openedchest:tier3
// wynntils:personal:discovery:territory
// wynntils:personal:saved_bookmarks_poi ???

public final class MapCategory {
    private final MapCategory parent;
    private final String name;
    private final String displayName;
    private final MapFeatureAttributes attributes;

    public MapCategory(MapCategory parent, String name, String displayName, MapFeatureAttributes attributes) {
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.attributes = attributes;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() + ":" : "") + name;
    }

    public MapCategory getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MapFeatureAttributes getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MapCategory) obj;
        return Objects.equals(this.parent, that.parent)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.displayName, that.displayName)
                && Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, displayName, attributes);
    }

    @Override
    public String toString() {
        return "MapCategory[" + "parent="
                + parent + ", " + "name="
                + name + ", " + "displayName="
                + displayName + ", " + "attributes="
                + attributes + ']';
    }
}

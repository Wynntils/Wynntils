package com.wynntils.models.mapdata;

public class MapCategory {
    private final MapCategory parent;
    private final String name;
    private final String displayName;

    // wynntils:lootrun:chest:tier1
    // wynntils:service:profession:scribing
    // wynntils:service:identifier
    // wynntils:npc:quest
    // wynntils:personal:lootrunpath
    // wynntils:personal:openedchest:tier3
    // wynntils:personal:discovery:territory
    // wynntils:personal:saved_bookmarks_poi ???

    public MapCategory(MapCategory parent, String name, String displayName) {
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
    }

    public MapCategory getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() + ":" : "") + name;
    }

    public String getDisplayName() {
        return displayName;
    }
}

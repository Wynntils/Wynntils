package com.wynntils.screens.maps.managers.type;

public enum OverrideType {
    MAP_LOCATION_OVERRIDE("Map Location override"),
    MAP_PATH_OVERRIDE("Map Path override"),
    MAP_AREA_OVERRIDE("Map Area override");

    private final String displayName;

    OverrideType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

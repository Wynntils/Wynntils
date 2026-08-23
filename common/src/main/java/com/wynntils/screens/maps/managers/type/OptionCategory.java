package com.wynntils.screens.maps.managers.type;

public enum OptionCategory {
    GENERAL("General"),
    LABEL("Label"),
    ICON("Icon"),
    MARKER("Marker"),
    AREA_BORDER("Area & Border");

    private final String displayName;

    OptionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
package com.wynntils.screens.maps.type;

public enum OptionCategory {
    GENERAL("General"),
    DISPLAY("Display"),
    PERFORMANCE("Performance"),
    ADVANCED("Advanced");

    private final String displayName;

    OptionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
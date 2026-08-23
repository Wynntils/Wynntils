package com.wynntils.screens.maps.managers.type;

import net.minecraft.network.chat.Component;

public enum OptionCategory {
    GENERAL("General", "screens.wynntils.map.managers.categoryManager.optionCategory.general"),
    LABEL("Label", "screens.wynntils.map.managers.categoryManager.optionCategory.label"),
    ICON("Icon", "screens.wynntils.map.managers.categoryManager.optionCategory.icon"),
    MARKER("Marker", "screens.wynntils.map.managers.categoryManager.optionCategory.marker"),
    AREA_BORDER("Area & Border", "screens.wynntils.map.managers.categoryManager.optionCategory.AreaAndBorder");

    private final String name;
    private final String displayName;

    OptionCategory(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return Component.translatable(displayName);
    }
}
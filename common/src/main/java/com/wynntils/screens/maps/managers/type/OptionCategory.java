/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.type;

import net.minecraft.network.chat.Component;

public enum OptionCategory {
    GENERAL("general"),
    LABEL("label"),
    ICON("icon"),
    MARKER("marker"),
    AREA_BORDER("areaAndBorder");

    private static final String TRANSLATION_KEY_PREFIX = "screens.wynntils.map.managers.categoryManager.optionCategory.";

    private final String translationKey;

    OptionCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(TRANSLATION_KEY_PREFIX + translationKey);
    }
}
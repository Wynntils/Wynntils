/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.festival.type;

public enum FestivalType {
    HEROES("Festival of the Heroes"),
    BONFIRE("Festival of the Bonfire"),
    SPIRITS("Festival of the Spirits"),
    BLIZZARD("Festival of the Blizzard");

    private final String fullName;

    FestivalType(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}

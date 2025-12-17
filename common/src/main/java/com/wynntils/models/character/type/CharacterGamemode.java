/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

public enum CharacterGamemode {
    HUNTED("hunted"),
    IRONMAN("ironman"),
    ULTIMATE_IRONMAN("ultimate_ironman"),
    CRAFTSMAN("craftsman"),
    HARDCORE("hardcore");

    private final String apiName;

    CharacterGamemode(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }

    public static CharacterGamemode fromApiName(String apiName) {
        for (CharacterGamemode gamemode : values()) {
            if (gamemode.getApiName().equals(apiName)) {
                return gamemode;
            }
        }

        return null;
    }
}

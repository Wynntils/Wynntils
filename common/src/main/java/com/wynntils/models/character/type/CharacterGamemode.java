/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import java.util.Objects;

public enum CharacterGamemode {
    HUNTED("hunted", "\uE028", "\ue024"),
    IRONMAN("ironman", "\uE029", "\ue021"),
    ULTIMATE_IRONMAN("ultimate_ironman", "\uE083", "\ue022"),
    CRAFTSMAN("craftsman", "\uE026", "\ue023"),
    HARDCORE("hardcore", "\uE027", "\ue020");

    private final String apiName;
    private final String icon;
    private final String selectionIcon;

    CharacterGamemode(String apiName, String icon, String selectionIcon) {
        this.apiName = apiName;
        this.icon = icon;
        this.selectionIcon = selectionIcon;
    }

    public String getApiName() {
        return apiName;
    }

    public String getIcon() {
        return icon;
    }

    public String getSelectionIcon() {
        return selectionIcon;
    }

    public static CharacterGamemode fromApiName(String apiName) {
        for (CharacterGamemode gamemode : values()) {
            if (gamemode.getApiName().equals(apiName)) {
                return gamemode;
            }
        }
        return null;
    }

    public static CharacterGamemode fromIcon(String icon) {
        for (CharacterGamemode gamemode : values()) {
            if (Objects.equals(gamemode.getIcon(), icon)) {
                return gamemode;
            }
        }
        return null;
    }

    public static CharacterGamemode fromSelectedIcon(String icon) {
        for (CharacterGamemode gamemode : values()) {
            if (Objects.equals(gamemode.getSelectionIcon(), icon)) {
                return gamemode;
            }
        }
        return null;
    }
}

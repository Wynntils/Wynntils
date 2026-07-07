/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.colors.CustomColor;

public enum WardType {
    PURPLE("screens.wynntils.wynntilsGuides.obtain.raid", CustomColor.fromHexString("9a21bf")),
    BLUE("screens.wynntils.wynntilsGuides.obtain.raid", CustomColor.fromHexString("6977c1")),
    RED("screens.wynntils.wynntilsGuides.obtain.raid", CustomColor.fromHexString("f02e2e")),
    YELLOW("screens.wynntils.wynntilsGuides.obtain.raid", CustomColor.fromHexString("e0bf4b")),
    GREEN("screens.wynntils.wynntilsGuides.obtain.lootrun", CustomColor.fromHexString("94b937")),
    ORANGE("screens.wynntils.wynntilsGuides.obtain.lootrun", CustomColor.fromHexString("db7242")),
    PINK("screens.wynntils.wynntilsGuides.obtain.lootrun", CustomColor.fromHexString("d56ea5"));

    private final CustomColor color;
    private final String obtainSourceTranslationKey;

    WardType(String obtainSourceTranslationKey, CustomColor color) {
        this.color = color;
        this.obtainSourceTranslationKey = obtainSourceTranslationKey;
    }

    public CustomColor getColor() {
        return color;
    }

    public String getObtainSourceTranslationKey() {
        return obtainSourceTranslationKey;
    }

    public static WardType fromName(String name) {
        for (WardType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}

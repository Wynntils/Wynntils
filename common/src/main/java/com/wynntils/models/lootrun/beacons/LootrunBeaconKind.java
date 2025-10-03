/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.beacons;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.config.NullableConfig;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public enum LootrunBeaconKind implements BeaconKind, NullableConfig {
    GREEN(CustomColor.fromInt(0x00FF80), CommonColors.GREEN),
    YELLOW(CustomColor.fromInt(0xFFFF33), CommonColors.YELLOW),
    BLUE(CustomColor.fromInt(0x5C5CE6), CommonColors.BLUE),
    PURPLE(CustomColor.fromInt(0xFF00FF), CommonColors.PURPLE),
    GRAY(CustomColor.fromInt(0xBFBFBF), CommonColors.LIGHT_GRAY),
    ORANGE(CustomColor.fromInt(0xFF9500), CommonColors.ORANGE),
    RED(CustomColor.fromInt(0xFF0000), CommonColors.RED),
    DARK_GRAY(CustomColor.fromInt(0x808080), CommonColors.GRAY),
    WHITE(CommonColors.WHITE, CommonColors.WHITE),
    AQUA(CustomColor.fromInt(0x55FFFF), CommonColors.AQUA),
    CRIMSON(CustomColor.fromInt(0xF010), CommonColors.GRADIENT_2),
    RAINBOW(CustomColor.fromInt(0x00F000), CommonColors.RAINBOW);

    // These values are used to identify the beacon kind
    private final CustomColor customColor;

    // This value is used to render the beacon kind in Wynntils
    private final CustomColor displayColor;

    LootrunBeaconKind(CustomColor customColor, CustomColor displayColor) {
        this.customColor = customColor;
        this.displayColor = displayColor;
    }

    @Override
    public CustomColor getCustomColor() {
        return customColor;
    }

    @Override
    public float getCustomModelData() {
        return Services.CustomModel.getFloat(Models.Beacon.BEACON_COLOR_CUSTOM_MODEL_DATA_KEY)
                .orElse(-1f);
    }

    public static LootrunBeaconKind fromName(String name) {
        for (LootrunBeaconKind color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }

        return null;
    }

    public static LootrunBeaconKind fromColor(CustomColor color) {
        for (LootrunBeaconKind beaconKind : values()) {
            if (beaconKind.getCustomColor().equals(color)) {
                return beaconKind;
            }
        }

        return null;
    }

    public CustomColor getDisplayColor() {
        return displayColor;
    }
}

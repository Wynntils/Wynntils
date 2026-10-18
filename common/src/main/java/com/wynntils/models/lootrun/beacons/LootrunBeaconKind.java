/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.beacons;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.config.NullableConfig;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.type.Pair;

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
    PINK(CustomColor.fromInt(0xFD72B1), CustomColor.fromInt(0xFD72B1)),
    CRIMSON(CustomColor.fromInt(0xF010), WynncraftShaderColor.GRADIENT_2.color),
    RAINBOW(CustomColor.fromInt(0x00F000), WynncraftShaderColor.RAINBOW.color),
    // Used by the "Route Indigo" lootrun mission and the "+30 Pulls" trial, where Blue and Purple
    // beacons are hidden behind a special "Obscured" beacon. Without this entry the beacon could not
    // be identified at all (fromColor returned null), so none of the custom beacon handling applied.
    OBSCURED(CustomColor.fromInt(0x00F018), WynncraftShaderColor.FADE_2.color),
    // Route Indigo obscures both the Blue and the Purple beacon, so two obscured beacons can be
    // offered at the same time. Since they share the exact same color they cannot be told apart by
    // color, and everything in LootrunBeaconModel is keyed by kind, so the second obscured beacon
    // the model sees gets this extra slot. This value is never matched from a color or a chat
    // message; it only exists to give the second obscured beacon its own entry in the beacon map.
    OBSCURED_2(CustomColor.fromInt(0x00F018), WynncraftShaderColor.FADE_2.color);

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
    public Pair<Float, Float> getCustomModelData() {
        return Services.CustomModel.getRange(Models.Beacon.BEACON_COLOR_CUSTOM_MODEL_DATA_KEY)
                .orElse(Pair.of(-1f, -1f));
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

    /**
     * The name shown to the user. Both obscured slots represent the same in-game beacon, so they
     * share a name.
     */
    public String getDisplayName() {
        return this == OBSCURED_2 ? OBSCURED.getDisplayName() : EnumUtils.toNiceString(this);
    }

    /** Whether this kind is one of the slots used to track the "Obscured" beacon. */
    public boolean isObscured() {
        return this == OBSCURED || this == OBSCURED_2;
    }
}

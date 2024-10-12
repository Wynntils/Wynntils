/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.beacons;

import com.wynntils.core.components.Models;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;

public enum LootrunBeaconKind implements BeaconKind {
    GREEN(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x00FF80), CommonColors.GREEN),
    YELLOW(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xFFFF33), CommonColors.YELLOW),
    BLUE(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x5C5CE6), CommonColors.BLUE),
    PURPLE(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xFF00FF), CommonColors.PURPLE),
    GRAY(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xBFBFBF), CommonColors.LIGHT_GRAY),
    ORANGE(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xFF9500), CommonColors.ORANGE),
    RED(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xFF0000), CommonColors.RED),
    DARK_GRAY(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x808080), CommonColors.GRAY),
    WHITE(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CommonColors.WHITE, CommonColors.WHITE),
    AQUA(Models.Beacon.COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x55FFFF), CommonColors.AQUA),
    RAINBOW(80, CommonColors.WHITE, CommonColors.RAINBOW);

    // These values are used to identify the beacon kind
    private final int customModelData;
    private final CustomColor customColor;

    // This value is used to render the beacon kind in Wynntils
    private final CustomColor displayColor;

    LootrunBeaconKind(int customModelData, CustomColor customColor, CustomColor displayColor) {
        this.customModelData = customModelData;
        this.customColor = customColor;
        this.displayColor = displayColor;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        if (itemStack.getItem() != Items.POTION) return false;

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return false;

        CustomModelData potionCustomModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (potionCustomModelData == null) return false;

        int customModel = potionCustomModelData.value();
        int potionCustomColor = potionContents.customColor().orElse(CommonColors.WHITE.asInt());

        return this.customModelData == customModel && this.customColor.equals(CustomColor.fromInt(potionCustomColor));
    }

    public static LootrunBeaconKind fromName(String name) {
        for (LootrunBeaconKind color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }

        return null;
    }

    public CustomColor getDisplayColor() {
        return displayColor;
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.EnumSet;

public enum WardType {
    PURPLE(EnumSet.of(ItemObtainType.RAID), CustomColor.fromHexString("9a21bf")),
    BLUE(EnumSet.of(ItemObtainType.RAID), CustomColor.fromHexString("6977c1")),
    RED(EnumSet.of(ItemObtainType.RAID), CustomColor.fromHexString("f02e2e")),
    YELLOW(EnumSet.of(ItemObtainType.RAID), CustomColor.fromHexString("e0bf4b")),
    GREEN(EnumSet.of(ItemObtainType.LOOTRUN), CustomColor.fromHexString("94b937")),
    ORANGE(EnumSet.of(ItemObtainType.LOOTRUN), CustomColor.fromHexString("db7242")),
    PINK(EnumSet.of(ItemObtainType.LOOTRUN), CustomColor.fromHexString("d56ea5"));

    private final CustomColor color;
    private final EnumSet<ItemObtainType> itemObtainTypes;

    WardType(EnumSet<ItemObtainType> itemObtainTypes, CustomColor color) {
        this.color = color;
        this.itemObtainTypes = itemObtainTypes;
    }

    public CustomColor getColor() {
        return color;
    }

    public EnumSet<ItemObtainType> getItemObtainTypes() {
        return itemObtainTypes;
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

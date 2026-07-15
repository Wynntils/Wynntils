/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.utils.colors.CustomColor;

public enum WardType {
    PURPLE(ItemObtainType.RAID, CustomColor.fromHexString("9a21bf")),
    BLUE(ItemObtainType.RAID, CustomColor.fromHexString("6977c1")),
    RED(ItemObtainType.RAID, CustomColor.fromHexString("f02e2e")),
    YELLOW(ItemObtainType.RAID, CustomColor.fromHexString("e0bf4b")),
    GREEN(ItemObtainType.LOOTRUN, CustomColor.fromHexString("94b937")),
    ORANGE(ItemObtainType.LOOTRUN, CustomColor.fromHexString("db7242")),
    PINK(ItemObtainType.LOOTRUN, CustomColor.fromHexString("d56ea5"));

    private final CustomColor color;
    private final ItemObtainType itemObtainType;

    WardType(ItemObtainType itemObtainType, CustomColor color) {
        this.color = color;
        this.itemObtainType = itemObtainType;
    }

    public CustomColor getColor() {
        return color;
    }

    public ItemObtainType getItemObtainType() {
        return itemObtainType;
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

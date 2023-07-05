/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum ContentStatus {
    STARTED("a", Items.GOLDEN_AXE),
    AVAILABLE("e", Items.GOLDEN_AXE),
    UNAVAILABLE("c", Items.GOLDEN_AXE),
    COMPLETED("a", Items.GOLDEN_PICKAXE);

    private final String colorCode;
    private final Item item;

    ContentStatus(String colorCode, Item item) {
        this.colorCode = colorCode;
        this.item = item;
    }

    public static ContentStatus from(String colorCode, Item item) {
        for (ContentStatus status : values()) {
            if (status.getColorCode().equals(colorCode) && status.getItem().equals(item)) return status;
        }

        return null;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Item getItem() {
        return item;
    }
}

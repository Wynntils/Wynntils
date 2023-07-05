/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum ContentStatus {
    STARTED(ChatFormatting.GREEN.getChar(), Items.GOLDEN_AXE),
    AVAILABLE(ChatFormatting.YELLOW.getChar(), Items.GOLDEN_AXE),
    UNAVAILABLE(ChatFormatting.RED.getChar(), Items.GOLDEN_AXE),
    COMPLETED(ChatFormatting.GREEN.getChar(), Items.GOLDEN_PICKAXE);

    private final char colorCode;
    private final Item item;

    ContentStatus(char colorCode, Item item) {
        this.colorCode = colorCode;
        this.item = item;
    }

    public static ContentStatus from(char colorCode, Item item) {
        for (ContentStatus status : values()) {
            if ((status.getColorCode() == colorCode) && status.getItem().equals(item)) return status;
        }

        return null;
    }

    public char getColorCode() {
        return colorCode;
    }

    public Item getItem() {
        return item;
    }
}

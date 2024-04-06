/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum ActivityStatus {
    STARTED(ChatFormatting.GREEN.getChar(), Items.GOLDEN_AXE),
    AVAILABLE(ChatFormatting.YELLOW.getChar(), Items.GOLDEN_AXE),
    UNAVAILABLE(ChatFormatting.RED.getChar(), Items.GOLDEN_AXE),
    COMPLETED(ChatFormatting.GREEN.getChar(), Items.GOLDEN_PICKAXE);

    private final char colorCode;
    private final Item item;

    ActivityStatus(char colorCode, Item item) {
        this.colorCode = colorCode;
        this.item = item;
    }

    public static ActivityStatus from(char colorCode, Item item) {
        for (ActivityStatus status : values()) {
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

    public Component getQuestStateComponent() {
        return switch (this) {
            case STARTED -> Component.literal("Started...").withStyle(ChatFormatting.YELLOW);
            case AVAILABLE -> Component.literal("Can start...").withStyle(ChatFormatting.YELLOW);
            case UNAVAILABLE -> Component.literal("Cannot start...").withStyle(ChatFormatting.RED);
            case COMPLETED -> Component.literal("Completed!").withStyle(ChatFormatting.GREEN);
        };
    }
}

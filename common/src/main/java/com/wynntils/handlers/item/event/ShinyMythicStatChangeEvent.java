/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ShinyMythicStatChangeEvent extends Event {
    private final String shinyStatName;
    private final long previousStat;
    private final long newStat;
    private final ItemStack itemStack;

    public ShinyMythicStatChangeEvent(ItemStack itemStack, String shinyStatName, long previousStat, long newStat) {
        this.itemStack = itemStack;
        this.shinyStatName = shinyStatName;
        this.previousStat = previousStat;
        this.newStat = newStat;
    }

    public String getShinyStatName() {
        return shinyStatName;
    }

    public long getPreviousStat() {
        return previousStat;
    }

    public long getNewStat() {
        return newStat;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}

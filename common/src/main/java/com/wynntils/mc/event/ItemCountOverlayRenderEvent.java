/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.item.ItemStack;

public final class ItemCountOverlayRenderEvent extends BaseEvent {
    private final ItemStack itemStack;
    private String countString;
    private int countColor;

    public ItemCountOverlayRenderEvent(ItemStack itemStack, String countString, int countColor) {
        this.itemStack = itemStack;
        this.countString = countString;
        this.countColor = countColor;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getCountString() {
        return countString;
    }

    public void setCountString(String countString) {
        this.countString = countString;
    }

    public int getCountColor() {
        return countColor;
    }

    public void setCountColor(int countColor) {
        this.countColor = countColor;
    }
}

/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;

/** Fired on click in a container */
@Cancelable
public class ContainerClickEvent extends WynntilsEvent {
    private final int containerId;
    private final int slotNum;
    private final ItemStack itemStack;
    private final ClickType clickType;
    private final int buttonNum;

    public ContainerClickEvent(int containerId, int slotNum, ItemStack itemStack, ClickType clickType, int buttonNum) {
        this.containerId = containerId;
        this.slotNum = slotNum;
        this.itemStack = itemStack;
        this.clickType = clickType;
        this.buttonNum = buttonNum;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getSlotNum() {
        return slotNum;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public int getButtonNum() {
        return buttonNum;
    }
}

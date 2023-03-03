/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fired on click in a container */
@Cancelable
public class ContainerClickEvent extends Event {
    private final AbstractContainerMenu containerMenu;
    private final int slotNum;
    private final ClickType clickType;
    private final int buttonNum;

    public ContainerClickEvent(AbstractContainerMenu containerMenu, int slotNum, ClickType clickType, int buttonNum) {
        this.containerMenu = containerMenu;
        this.slotNum = slotNum;
        this.clickType = clickType;
        this.buttonNum = buttonNum;
    }

    public AbstractContainerMenu getContainerMenu() {
        return containerMenu;
    }

    public int getSlotNum() {
        return slotNum;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public int getButtonNum() {
        return buttonNum;
    }

    public ItemStack getItemStack() {
        if (slotNum >= 0) {
            return containerMenu.getSlot(slotNum).getItem();
        } else {
            return ItemStack.EMPTY;
        }
    }
}

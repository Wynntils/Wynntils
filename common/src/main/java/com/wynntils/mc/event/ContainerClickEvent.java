/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

/** Fired on click in a container */
public final class ContainerClickEvent extends BaseEvent implements CancelRequestable {
    private final AbstractContainerMenu containerMenu;
    private final int slotNum;
    private final ClickType clickType;
    private final int mouseButton;

    public ContainerClickEvent(AbstractContainerMenu containerMenu, int slotNum, ClickType clickType, int mouseButton) {
        this.containerMenu = containerMenu;
        this.slotNum = slotNum;
        this.clickType = clickType;
        this.mouseButton = mouseButton;
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

    public int getMouseButton() {
        return mouseButton;
    }

    public ItemStack getItemStack() {
        if (slotNum >= 0) {
            return containerMenu.getSlot(slotNum).getItem();
        } else {
            return ItemStack.EMPTY;
        }
    }
}

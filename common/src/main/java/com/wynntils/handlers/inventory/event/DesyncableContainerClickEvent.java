/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.event;

import com.wynntils.handlers.inventory.resync.InventoryResyncStrategy;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.Event;

public class DesyncableContainerClickEvent extends Event {
    private final AbstractContainerMenu containerMenu;
    private final int slotNum;
    private final ClickType clickType;
    private final int mouseButton;

    private InventoryResyncStrategy resyncStrategy;
    private boolean shouldResync = false;

    public DesyncableContainerClickEvent(
            AbstractContainerMenu containerMenu,
            int slotNum,
            ClickType clickType,
            int mouseButton,
            InventoryResyncStrategy resyncStrategy) {
        this.containerMenu = containerMenu;
        this.slotNum = slotNum;
        this.clickType = clickType;
        this.mouseButton = mouseButton;
        this.resyncStrategy = resyncStrategy;
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

    public InventoryResyncStrategy getResyncStrategy() {
        return resyncStrategy;
    }

    public void setResyncStrategy(InventoryResyncStrategy resyncStrategy) {
        this.resyncStrategy = resyncStrategy;
    }

    public boolean getShouldResync() {
        return shouldResync;
    }

    public void setShouldResync(boolean shouldResync) {
        this.shouldResync = shouldResync;
    }
}

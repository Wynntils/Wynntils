/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.utils.ContainerUtils;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.Objects;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerInventoryModel extends Model {

    private int emeralds = 0;
    private int openSlots = 0;

    @Override
    public void init() {
        resetCache();
    }

    @Override
    public void disable() {
        resetCache();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            updateCache();
        } else {
            resetCache();
        }
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        // Only update if the container is the player inventory
        if (e.getContainerId() == McUtils.player().inventoryMenu.containerId) {
            updateCache();
        }
    }

    @SubscribeEvent
    public void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.player().getInventory())) {
            updateCache();
        }
    }

    private void updateCache() {
        InventoryMenu inventory = McUtils.inventoryMenu();
        emeralds = ContainerUtils.getEmeraldCountInContainer(inventory);
        openSlots = InventoryUtils.getEmptySlots(McUtils.inventory());
    }

    private void resetCache() {
        emeralds = 0;
        openSlots = 0;
    }

    public int getCurrentEmeraldCount() {
        return emeralds;
    }

    public int getOpenInvSlots() {
        return openSlots;
    }

    public int getUsedInvSlots() {
        return 28 - openSlots;
    }
}

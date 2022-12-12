/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.Objects;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerInventoryModel extends Model {

    private static int emeralds = 0;
    private static int openSlots = 0;

    public static void init() {
        resetCache();
    }

    public static void disable() {
        resetCache();
    }

    @SubscribeEvent
    public static void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            updateCache();
        } else {
            resetCache();
        }
    }

    @SubscribeEvent
    public static void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        // Only update if the container is the player inventory
        if (e.getContainerId() == McUtils.player().inventoryMenu.containerId) {
            updateCache();
        }
    }

    @SubscribeEvent
    public static void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.player().getInventory())) {
            updateCache();
        }
    }

    private static void updateCache() {
        InventoryMenu inventory = McUtils.inventoryMenu();
        emeralds = ContainerUtils.getEmeraldCountInContainer(inventory);
        openSlots = ContainerUtils.getEmptySlotsInContainer(inventory);
    }

    private static void resetCache() {
        emeralds = 0;
        openSlots = 0;
    }

    public static int getCurrentEmeraldCount() {
        return emeralds;
    }

    public static int getOpenInvSlots() {
        return openSlots;
    }
}

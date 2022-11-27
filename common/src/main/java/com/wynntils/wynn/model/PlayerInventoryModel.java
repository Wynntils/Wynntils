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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerInventoryModel extends Model {

    private static int emeralds = -1;
    private static int openSlots = -1;

    public static void init() {}

    @SubscribeEvent
    public static void worldChange(WorldStateEvent e) {
        InventoryMenu inventory = McUtils.inventoryMenu();
        emeralds = ContainerUtils.getEmeraldCountInContainer(inventory);
        openSlots = ContainerUtils.getEmptySlotsInContainer(inventory);
    }

    @SubscribeEvent
    public static void containerEvent(ContainerSetContentEvent e) {
        InventoryMenu inventory = McUtils.inventoryMenu();
        emeralds = ContainerUtils.getEmeraldCountInContainer(inventory);
        openSlots = ContainerUtils.getEmptySlotsInContainer(inventory);
    }

    @SubscribeEvent
    public static void slotEvent(SetSlotEvent e) {
        InventoryMenu inventory = McUtils.inventoryMenu();
        emeralds = ContainerUtils.getEmeraldCountInContainer(inventory);
        openSlots = ContainerUtils.getEmptySlotsInContainer(inventory);
    }

    public static int getCurrentEmeraldCount() {
        return emeralds;
    }

    public static int getOpenInvSlots() {
        return openSlots;
    }
}

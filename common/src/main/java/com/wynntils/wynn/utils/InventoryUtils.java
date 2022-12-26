/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.parsers.EmeraldPouchParser;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtils {
    public static final int COMPASS_SLOT_NUM = 6;
    public static final int QUEST_BOOK_SLOT_NUM = 7;
    public static final int SOUL_POINTS_SLOT_NUM = 8;
    public static final int INGREDIENT_POUCH_SLOT_NUM = 13;

    public static List<EmeraldPouch> getEmeraldPouches(Container inventory) {
        List<EmeraldPouch> emeraldPouches = new ArrayList<>();

        for (int slotNumber = 0; slotNumber < inventory.getContainerSize(); slotNumber++) {
            ItemStack stack = inventory.getItem(slotNumber);
            if (!stack.isEmpty() && WynnItemMatchers.isEmeraldPouch(stack)) {
                emeraldPouches.add(new EmeraldPouch(slotNumber, stack));
            }
        }
        return emeraldPouches;
    }

    public static void sendInventorySlotMouseClick(int slotNumber, ItemStack stack, MouseClickType mouseButton) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(slotNumber, stack);

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                McUtils.inventoryMenu().containerId,
                McUtils.inventoryMenu().getStateId(),
                slotNumber,
                mouseButton.ordinal(),
                ClickType.PICKUP,
                ItemStack.EMPTY,
                changedSlots));
    }

    public static int findHorseSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum <= 44; slotNum++) {
            ItemStack stack = inventory.getItem(slotNum);
            if (WynnItemMatchers.isHorse(stack)) {
                return slotNum;
            }
        }
        return -1;
    }

    public static int getEmptySlots(Inventory inventory) {
        if (inventory == null) return 0;
        int slots = 0;
        for (ItemStack itemStack : inventory.items) {
            if (itemStack.isEmpty()) slots++;
        }
        return slots;
    }

    public enum MouseClickType {
        LEFT_CLICK,
        RIGHT_CLICK
    }

    public static final class EmeraldPouch {
        private final int slotNumber;
        private final ItemStack stack;

        private EmeraldPouch(int slotNumber, ItemStack stack) {
            this.slotNumber = slotNumber;
            this.stack = stack;
        }

        public int getSlotNumber() {
            return slotNumber;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getUsage() {
            return EmeraldPouchParser.getPouchUsage(stack);
        }

        public boolean isEmpty() {
            return getUsage() == 0;
        }

        public int getCapacity() {
            return EmeraldPouchParser.getPouchCapacity(stack);
        }
    }
}

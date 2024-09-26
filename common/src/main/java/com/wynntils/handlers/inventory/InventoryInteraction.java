/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;

public sealed interface InventoryInteraction {
    record PickUp(int slotNum, ItemStack pickedUp, ItemStack leftInSlot) implements InventoryInteraction {}

    record PickUpAll(ItemStack pickedUp) implements InventoryInteraction {}

    record Place(int slotNum, ItemStack placed) implements InventoryInteraction {}

    record Spread(IntList slots, ItemStack stack, boolean single) implements InventoryInteraction {}

    record Swap(int slotNum, ItemStack placed, ItemStack pickedUp) implements InventoryInteraction {}

    record ThrowFromHeld(ItemStack thrown) implements InventoryInteraction {}

    record ThrowFromSlot(int slotNum, ItemStack thrown) implements InventoryInteraction {}

    record Transfer(int slotNum, ItemStack transferred) implements InventoryInteraction {}
}

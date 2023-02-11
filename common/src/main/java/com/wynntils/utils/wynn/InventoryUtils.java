/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtils {
    public static final int COMPASS_SLOT_NUM = 6;
    public static final int QUEST_BOOK_SLOT_NUM = 7;
    public static final int SOUL_POINTS_SLOT_NUM = 8;
    public static final int INGREDIENT_POUCH_SLOT_NUM = 13;

    public static void sendInventorySlotMouseClick(int slotNumber, MouseClickType mouseButton) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        ItemStack itemStack = McUtils.inventory().getItem(slotNumber);
        changedSlots.put(slotNumber, itemStack);

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                McUtils.inventoryMenu().containerId,
                McUtils.inventoryMenu().getStateId(),
                slotNumber,
                mouseButton.ordinal(),
                ClickType.PICKUP,
                ItemStack.EMPTY,
                changedSlots));
    }

    public static int getEmptySlots(Inventory inventory) {
        if (inventory == null) return 0;
        int slots = 0;
        for (ItemStack itemStack : inventory.items) {
            if (itemStack.isEmpty()) slots++;
        }
        return slots;
    }

    public static boolean isWeapon(ItemStack itemStack) {
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isPresent()) {
            return gearItemOpt.get().getGearInfo().type().isWeapon();
        }

        Optional<CraftedGearItem> craftedGearItemOpt = Models.Item.asWynnItem(itemStack, CraftedGearItem.class);
        if (craftedGearItemOpt.isPresent()) {
            // FIXME: We do not have gear type recognition for crafted gear
            // so for the moment, we assume all crafted items are weapon
            // return craftedGearItemOpt.get().getGearType().isWeapon();
            return true;
        }

        return false;
    }

    public enum MouseClickType {
        LEFT_CLICK,
        RIGHT_CLICK
    }
}

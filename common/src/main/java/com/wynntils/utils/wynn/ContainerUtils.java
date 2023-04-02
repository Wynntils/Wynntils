/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ContainerUtils {
    public static NonNullList<ItemStack> getItems(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().getItems();
        }

        // Defensive programming, should not really happen
        return NonNullList.create();
    }

    public static boolean openInventory(int slotNum) {
        int containerId = McUtils.containerMenu().containerId;
        if (containerId != 0) {
            // Another inventory is already open, cannot do this
            return false;
        }
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(slotNum));
        McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
        return true;
    }

    /**
     * Clicks on a slot in the specified container. containerId and the list of items should correspond to the
     * same container!
     */
    public static void clickOnSlot(int clickedSlot, int containerId, int mouseButton, List<ItemStack> items) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(clickedSlot, new ItemStack(Items.AIR));

        // FIXME: To expand usage of this function, the following variables needs to
        // be properly handled
        int transactionId = 0;

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButton,
                ClickType.PICKUP,
                items.get(clickedSlot),
                changedSlots));
    }

    public static void closeContainer(int containerId) {
        McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
    }
}

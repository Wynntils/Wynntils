/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ContainerUtils {
    private static final int INVENTORY_SLOTS = 36;

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

        NonNullList<ItemStack> items = McUtils.containerMenu().getItems();
        // We need to offset the slot number so that it corresponds to the correct slot in the inventory
        clickOnSlot(INVENTORY_SLOTS + slotNum, containerId, GLFW.GLFW_MOUSE_BUTTON_LEFT, items);

        return true;
    }

    public static void sendSlotInteraction(
            int containerId,
            int stateId,
            int slotNumber,
            ClickType clickType,
            int mouseButton,
            ItemStack heldStack,
            Int2ObjectMap<ItemStack> changedSlots) {
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId, stateId, slotNumber, mouseButton, clickType, heldStack, changedSlots));
    }

    public static void sendSlotInteraction(
            AbstractContainerMenu menu,
            int slotNumber,
            ClickType clickType,
            int mouseButton,
            ItemStack heldStack,
            Int2ObjectMap<ItemStack> changedSlots) {
        sendSlotInteraction(
                menu.containerId, menu.getStateId(), slotNumber, clickType, mouseButton, heldStack, changedSlots);
    }

    public static void sendSlotInteraction(
            AbstractContainerMenu menu, int slotNumber, ClickType clickType, int mouseButton) {
        sendSlotInteraction(menu, slotNumber, clickType, mouseButton, ItemStack.EMPTY, Int2ObjectMaps.emptyMap());
    }

    /**
     * Clicks on a slot in the specified container. containerId and the list of items should correspond to the
     * same container!
     */
    public static void clickOnSlot(int clickedSlot, int containerId, int mouseButton, List<ItemStack> items) {
        // FIXME: To expand usage of this function, the following variables needs to
        // be properly handled
        int transactionId = 0;

        sendSlotInteraction(
                containerId,
                transactionId,
                clickedSlot,
                ClickType.PICKUP,
                mouseButton,
                items.get(clickedSlot),
                Int2ObjectMaps.singleton(clickedSlot, ItemStack.EMPTY));
    }

    public static void shiftClickOnSlot(int clickedSlot, int containerId, int mouseButton, List<ItemStack> items) {
        int transactionId = 0;

        sendSlotInteraction(
                containerId,
                transactionId,
                clickedSlot,
                ClickType.QUICK_MOVE,
                mouseButton,
                items.get(clickedSlot),
                Int2ObjectMaps.singleton(clickedSlot, ItemStack.EMPTY));
    }

    public static void pressKeyOnSlot(int clickedSlot, int containerId, int buttonNum, List<ItemStack> items) {
        int transactionId = 0;

        sendSlotInteraction(
                containerId,
                transactionId,
                clickedSlot,
                ClickType.SWAP,
                buttonNum,
                items.get(clickedSlot),
                Int2ObjectMaps.singleton(clickedSlot, ItemStack.EMPTY));
    }

    public static void closeContainer(int containerId) {
        McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
    }

    /**
     * Closes invisible containers opened in the background, without closing the visible screen.
     */
    public static void closeBackgroundContainer() {
        McUtils.sendPacket(new ServerboundContainerClosePacket(McUtils.player().containerMenu.containerId));
        McUtils.player().containerMenu = McUtils.player().inventoryMenu;
    }
}

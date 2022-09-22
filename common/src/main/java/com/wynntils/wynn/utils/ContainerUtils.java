/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.parsers.EmeraldPouchParser;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ContainerUtils {
    public static boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen containerScreen
                && containerScreen.getTitle().getString().contains("Loot Chest ");
    }

    public static boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen)) return false;

        String title = screen.getTitle().getString();
        return title.startsWith("Loot Chest")
                || title.startsWith("Daily Rewards")
                || title.contains("Objective Rewards");
    }

    public static NonNullList<ItemStack> getItems(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().getItems();
        }

        // Defensive programming, should not really happen
        return NonNullList.create();
    }

    public static boolean openInventory(int slotNum) {
        int id = McUtils.containerMenu().containerId;
        if (id != 0) {
            // Another inventory is already open, cannot do this
            return false;
        }
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(slotNum));
        McUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
        return true;
    }

    public static void clickOnSlot(int clickedSlot, int containerId, List<ItemStack> items) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(clickedSlot, new ItemStack(Items.AIR));

        // FIXME: To expand usage of this function, the following variables needs to
        // be properly handled
        int mouseButtonNum = 0;
        int transactionId = 0;

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButtonNum,
                ClickType.PICKUP,
                items.get(clickedSlot),
                changedSlots));
    }

    public static int getEmeraldCountInContainer(AbstractContainerMenu containerMenu) {
        if (containerMenu == null) return 0;

        int emeralds = 0;

        for (ItemStack itemStack : containerMenu.getItems()) {
            if (itemStack.isEmpty()) continue;

            String displayName = ComponentUtils.getCoded(itemStack.getHoverName());
            if (itemStack.getItem() == Items.EMERALD && displayName.equals(ChatFormatting.GREEN + "Emerald")) {
                emeralds += itemStack.getCount();
            } else if (itemStack.getItem() == Items.EMERALD_BLOCK
                    && displayName.equals(ChatFormatting.GREEN + "Emerald Block")) {
                emeralds += itemStack.getCount() * 64;
            } else if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE
                    && displayName.equals(ChatFormatting.GREEN + "Liquid Emerald")) {
                emeralds += itemStack.getCount() * (64 * 64);
            } else if (WynnItemMatchers.isEmeraldPouch(itemStack)) {
                emeralds += EmeraldPouchParser.getPouchUsage(itemStack);
            }
        }

        return emeralds;
    }
}

/*
 * Copyright © Wynntils 2021, 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.emeralds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.utils.WynnItemMatchers;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Tools for retrieving information about emerald pouches */
public final class EmeraldManager extends Manager {
    private static final Pattern POUCH_USAGE_PATTERN =
            Pattern.compile("§6§l([\\d\\s]+)" + EmeraldSymbols.E_STRING + ".*");
    private static final Pattern POUCH_CAPACITY_PATTERN =
            Pattern.compile("\\((\\d+)(" + EmeraldSymbols.EB + "|" + EmeraldSymbols.LE + "|stx) Total\\)");
    public int emeralds = 0;

    public EmeraldManager() {
        super(List.of());
    }

    public int getCurrentEmeraldCount() {
        return emeralds;
    }

    public int getPouchUsage(ItemStack stack) {
        LinkedList<String> lore = ItemUtils.getLore(stack);
        if (lore.isEmpty()) return 0;

        Matcher usageMatcher = POUCH_USAGE_PATTERN.matcher(lore.get(0));
        if (!usageMatcher.matches()) return 0;

        return Integer.parseInt(usageMatcher.group(1).replaceAll("\\s", ""));
    }

    public int getPouchCapacity(ItemStack stack) {
        String lore = ItemUtils.getStringLore(stack);
        Matcher capacityMatcher = POUCH_CAPACITY_PATTERN.matcher(lore);
        if (!capacityMatcher.find()) {
            WynntilsMod.error(
                    "EmeraldPouchParser#getPouchCapacity was called on an ItemStack that wasn't an emerald pouch");
            return -1;
        }
        int capacity = Integer.parseInt(capacityMatcher.group(1)) * 64;
        if (capacityMatcher.group(2).equals(EmeraldSymbols.LE)) capacity *= 64;
        if (capacityMatcher.group(2).equals("stx")) capacity *= 4096;
        return capacity;
    }

    public int getEmeraldCountInContainer(AbstractContainerMenu containerMenu) {
        if (containerMenu == null) return 0;

        int emeralds = 0;

        for (ItemStack itemStack : containerMenu.getItems()) {
            if (itemStack.isEmpty()) continue;

            if (WynnItemMatchers.isEmeraldPouch(itemStack)) {
                emeralds += getPouchUsage(itemStack);
                continue;
            }

            Item item = itemStack.getItem();
            if (item != Items.EMERALD && item != Items.EMERALD_BLOCK && item != Items.EXPERIENCE_BOTTLE) {
                continue;
            }

            String displayName = ComponentUtils.getCoded(itemStack.getHoverName());
            if (item == Items.EMERALD && displayName.equals(ChatFormatting.GREEN + "Emerald")) {
                emeralds += itemStack.getCount();
            } else if (item == Items.EMERALD_BLOCK && displayName.equals(ChatFormatting.GREEN + "Emerald Block")) {
                emeralds += itemStack.getCount() * 64;
            } else if (item == Items.EXPERIENCE_BOTTLE && displayName.equals(ChatFormatting.GREEN + "Liquid Emerald")) {
                emeralds += itemStack.getCount() * (64 * 64);
            }
        }

        return emeralds;
    }

    public List<EmeraldPouch> getEmeraldPouches(Container inventory) {
        List<EmeraldPouch> emeraldPouches = new ArrayList<>();

        for (int slotNumber = 0; slotNumber < inventory.getContainerSize(); slotNumber++) {
            ItemStack stack = inventory.getItem(slotNumber);
            if (!stack.isEmpty() && WynnItemMatchers.isEmeraldPouch(stack)) {
                emeraldPouches.add(new EmeraldPouch(slotNumber, stack));
            }
        }
        return emeraldPouches;
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
        emeralds = getEmeraldCountInContainer(inventory);
    }

    private void resetCache() {
        emeralds = 0;
    }
}

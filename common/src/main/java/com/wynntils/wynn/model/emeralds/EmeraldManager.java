/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.emeralds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.objects.WorldState;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private int inventoryEmeralds = 0;
    private int containerEmeralds = 0;

    public EmeraldManager() {
        super(List.of());
    }

    public boolean isEmeraldPouch(ItemStack itemStack) {
        Optional<EmeraldPouchItem> itemOpt = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
        return !itemOpt.isEmpty();
    }

    public int getCurrentEmeraldCount() {
        return inventoryEmeralds;
    }

    public int getUsage(ItemStack stack) {
        LinkedList<String> lore = ItemUtils.getLore(stack);
        if (lore.isEmpty()) return 0;

        Matcher usageMatcher = POUCH_USAGE_PATTERN.matcher(lore.get(0));
        if (!usageMatcher.matches()) return 0;

        return Integer.parseInt(usageMatcher.group(1).replaceAll("\\s", ""));
    }

    public int getCapacity(ItemStack stack) {
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

    public int getEmeraldCountInCurrentContainer() {
        int emerals = getEmeraldCountInContainer(McUtils.containerMenu());
        if (McUtils.player().containerMenu.containerId != 0) {
            // Subtract emeralds from inventory to get amount that is only in the container
            inventoryEmeralds -= Managers.Emerald.getCurrentEmeraldCount();
        }

        return emerals;
    }

    private void updateContainerEmeraldCount() {
        containerEmeralds = getEmeraldCountInContainer(McUtils.containerMenu());
    }

    private int getEmeraldCountInContainer(AbstractContainerMenu containerMenu) {
        if (containerMenu == null) return 0;

        int emeralds = 0;

        for (ItemStack itemStack : containerMenu.getItems()) {
            if (itemStack.isEmpty()) continue;

            if (isEmeraldPouch(itemStack)) {
                emeralds += getUsage(itemStack);
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

    public List<EmeraldPouchSlot> getEmeraldPouchSlots(Container inventory) {
        List<EmeraldPouchSlot> emeraldPouchSlots = new ArrayList<>();

        for (int slotNumber = 0; slotNumber < inventory.getContainerSize(); slotNumber++) {
            ItemStack stack = inventory.getItem(slotNumber);
            if (!stack.isEmpty() && isEmeraldPouch(stack)) {
                emeraldPouchSlots.add(new EmeraldPouchSlot(slotNumber, stack));
            }
        }
        return emeraldPouchSlots;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldState.WORLD) {
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
        } else {
            updateContainerEmeraldCount();
        }
    }

    @SubscribeEvent
    public void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.player().getInventory())) {
            updateCache();
        } else {
            updateContainerEmeraldCount();
        }
    }

    private void updateCache() {
        InventoryMenu inventory = McUtils.inventoryMenu();
        inventoryEmeralds = getEmeraldCountInContainer(inventory);
    }

    private void resetCache() {
        inventoryEmeralds = 0;
    }
}

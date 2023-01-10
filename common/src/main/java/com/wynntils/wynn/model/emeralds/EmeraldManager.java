/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.emeralds;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import com.wynntils.wynn.objects.WorldState;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class EmeraldManager extends Manager {
    private int inventoryEmeralds = 0;
    private int containerEmeralds = 0;

    public EmeraldManager() {
        super(List.of());
    }

    public boolean isEmeraldPouch(ItemStack itemStack) {
        Optional<EmeraldPouchItem> itemOpt = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
        return itemOpt.isPresent();
    }

    public int getCurrentEmeraldCount() {
        return inventoryEmeralds;
    }

    private int getPouchValue(ItemStack stack) {
        Optional<EmeraldPouchItem> optPouchItem = Models.Item.asWynnItem(stack, EmeraldPouchItem.class);
        if (optPouchItem.isEmpty()) return 0;

        EmeraldPouchItem pouchItem = optPouchItem.get();
        return pouchItem.getValue();
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
                emeralds += getPouchValue(itemStack);
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

/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.emeralds;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.handleditems.WynnItem;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import com.wynntils.wynn.handleditems.properties.EmeraldValuedItemProperty;
import com.wynntils.wynn.objects.WorldState;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class EmeraldManager extends Manager {
    private int inventoryEmeralds = 0;
    private int containerEmeralds = 0;
    private int pouchContainerId = -1;

    public EmeraldManager() {
        super(List.of());
    }

    public boolean isEmeraldPouch(ItemStack itemStack) {
        Optional<EmeraldPouchItem> itemOpt = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
        return itemOpt.isPresent();
    }

    public int getAmountInInventory() {
        return inventoryEmeralds;
    }

    public int getAmountInContainer() {
        return containerEmeralds;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() != WorldState.WORLD) return;

        inventoryEmeralds = 0;
        containerEmeralds = 0;

        // Rescan inventory at login
        Inventory inventory = McUtils.player().getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            adjustBalance(inventory.getItem(i), 1, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSetSlot(SetSlotEvent.Pre event) {
        boolean isInventory = (event.getContainer() == McUtils.player().getInventory());
        if (pouchContainerId != -1 && !isInventory) return;

        // Subtract the outgoing object from our balance
        adjustBalance(event.getContainer().getItem(event.getSlot()), -1, isInventory);
        // And add the incoming value
        adjustBalance(event.getItem(), 1, isInventory);
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        String title = WynnUtils.normalizeBadString(e.getTitle().getString());
        if (title.equals("Emerald Pouch")) {
            pouchContainerId = e.getContainerId();
        } else {
            pouchContainerId = -1;
        }
    }

    @SubscribeEvent
    public void onMenuClosed(MenuEvent.MenuClosedEvent e) {
        containerEmeralds = 0;
        pouchContainerId = -1;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        int containerStop;

        inventoryEmeralds = 0;
        containerEmeralds = 0;

        List<ItemStack> items = event.getItems();
        if (event.getContainerId() == 0) {
            containerStop = 0;
        } else if (event.getContainerId() == McUtils.player().containerMenu.containerId) {
            containerStop = items.size() - 36;
        } else {
            return;
        }

        for (int i = 0; i < containerStop; i++) {
            adjustBalance(items.get(i), 1, false);
        }
        for (int i = containerStop; i < items.size(); i++) {
            adjustBalance(items.get(i), 1, true);
        }
        if (event.getContainerId() == pouchContainerId) {
            // Don't count the emeralds in the pouch container twice, it's actually still
            // just the inventory
            containerEmeralds = 0;
        }
    }

    private void adjustBalance(ItemStack itemStack, int multiplier, boolean isInventory) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof EmeraldValuedItemProperty valuedItem)) return;

        int adjustValue = valuedItem.getEmeraldValue() * multiplier;
        if (isInventory) {
            inventoryEmeralds += adjustValue;
        } else {
            containerEmeralds += adjustValue;
        }
    }
}

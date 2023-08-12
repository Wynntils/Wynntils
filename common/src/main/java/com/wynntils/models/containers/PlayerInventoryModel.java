/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.type.InventoryWatcher;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerInventoryModel extends Model {
    private static final int MAX_INVENTORY_SLOTS = 28;
    private static final int MAX_INGREDIENT_POUCH_SLOTS = 27;

    private final InventoryWatcher emptySlotWatcher = new InventoryWatcher(ItemStack::isEmpty);
    private final List<InventoryWatcher> watchers = new ArrayList<>(List.of(emptySlotWatcher));

    public PlayerInventoryModel() {
        super(List.of());
    }

    public CappedValue getInventorySlots() {
        return new CappedValue(MAX_INVENTORY_SLOTS - getEmptySlots(), MAX_INVENTORY_SLOTS);
    }

    public CappedValue getIngredientPouchSlots() {
        return new CappedValue(getUsedIngredientPouchSlots(), MAX_INGREDIENT_POUCH_SLOTS);
    }

    public void registerWatcher(InventoryWatcher watcher) {
        watchers.add(watcher);
        updateCache();
    }

    public void unregisterWatcher(InventoryWatcher watcher) {
        watchers.remove(watcher);
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
        if (e.getContainerId() == McUtils.inventoryMenu().containerId) {
            updateCache();
        }
    }

    @SubscribeEvent
    public void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.inventory())) {
            updateCache();
        }
    }

    private int getEmptySlots() {
        return emptySlotWatcher.getSlots();
    }

    private int getUsedIngredientPouchSlots() {
        ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);
        Optional<IngredientPouchItem> pouchItemOpt = Models.Item.asWynnItem(itemStack, IngredientPouchItem.class);

        // This should never happen
        if (pouchItemOpt.isEmpty()) return -1;

        return pouchItemOpt.get().getIngredients().size();
    }

    private void updateCache() {
        Inventory inventory = McUtils.inventory();
        if (inventory == null) return;

        watchers.forEach(watcher -> {
            int slots = 0;
            int totalCount = 0;

            for (ItemStack itemStack : inventory.items) {
                if (watcher.shouldInclude(itemStack)) {
                    slots++;
                    totalCount += itemStack.getCount();
                }
            }

            if (slots != watcher.getSlots() || totalCount != watcher.getTotalCount()) {
                watcher.updateFromModel(slots, totalCount);
            }
        });
    }

    private void resetCache() {
        watchers.forEach(watcher -> watcher.updateFromModel(0, 0));
    }
}

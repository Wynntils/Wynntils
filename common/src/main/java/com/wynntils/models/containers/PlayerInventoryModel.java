/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerInventoryModel extends Model {
    private static final int MAX_INVENTORY_SLOTS = 28;
    private static final int MAX_INGREDIENT_POUCH_SLOTS = 27;

    private int emptySlots = 0;

    public PlayerInventoryModel() {
        super(List.of());
    }

    public CappedValue getInventorySlots() {
        return new CappedValue(MAX_INVENTORY_SLOTS - emptySlots, MAX_INVENTORY_SLOTS);
    }

    public CappedValue getIngredientPouchSlots() {
        return new CappedValue(getUsedIngredientPouchSlots(), MAX_INGREDIENT_POUCH_SLOTS);
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
        }
    }

    @SubscribeEvent
    public void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.player().getInventory())) {
            updateCache();
        }
    }

    private int getUsedIngredientPouchSlots() {
        ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);
        Optional<IngredientPouchItem> pouchItemOpt = Models.Item.asWynnItem(itemStack, IngredientPouchItem.class);

        // This should never happen
        if (pouchItemOpt.isEmpty()) return -1;

        return pouchItemOpt.get().getIngredients().size();
    }

    private void updateCache() {
        emptySlots = InventoryUtils.getEmptySlots(McUtils.inventory());
    }

    private void resetCache() {
        emptySlots = 0;
    }
}

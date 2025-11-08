/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.inventory;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class InventoryModel extends Model {
    private static final int MAX_INVENTORY_SLOTS = 29;
    private static final List<String> AUTO_CASTER_MAJOR_IDS = List.of("Sorcery", "Madness");

    private final InventoryWatcher emptySlotWatcher = new InventoryWatcher(ItemStack::isEmpty);
    private final List<InventoryWatcher> watchers = new ArrayList<>(List.of(emptySlotWatcher));

    public InventoryModel() {
        super(List.of());
    }

    public CappedValue getInventorySlots() {
        return new CappedValue(MAX_INVENTORY_SLOTS - getEmptySlots(), MAX_INVENTORY_SLOTS);
    }

    public void registerWatcher(InventoryWatcher watcher) {
        watchers.add(watcher);
        updateCache();
    }

    public void unregisterWatcher(InventoryWatcher watcher) {
        watchers.remove(watcher);
    }

    /**
     * @return List of all equipped armor, accessories, and held item that meets requirements
     */
    public List<ItemStack> getEquippedItems() {
        List<ItemStack> returnable = new ArrayList<>(McUtils.inventory().armor);
        Collections.reverse(returnable); // Reverse so that helmet is first

        for (int i : InventoryAccessory.getSlots()) {
            int baseSize = 0;
            if (McUtils.player().hasContainerOpen()) {
                // Scale according to server chest size
                // Eg. 3 row chest size = 27 (ends on i=26 since 0-index), we would get accessory slots {27, 28, 29, 30}
                baseSize = McUtils.player().containerMenu.getItems().size();
            }
            ItemStack accessory = McUtils.inventory().getItem(i + baseSize);
            if (ItemUtils.isEmptyAccessorySlot(accessory)) continue;
            returnable.add(McUtils.inventory().getItem(i + baseSize));
        }

        Optional<GearItem> handItemOpt =
                Models.Item.asWynnItem(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND), GearItem.class);
        if (handItemOpt.isPresent()
                && handItemOpt.get().meetsActualRequirements()
                && handItemOpt.get().getGearType().isWeapon()) {
            returnable.add(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND));
        }

        return returnable.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
    }

    /**
     * @return True if the player has any item with AUTO_CASTER_MAJOR_IDS major id(s)
     */
    public boolean hasAutoCasterItem() {
        for (ItemStack item : getEquippedItems()) {
            Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(item, GearItem.class);
            if (gearItemOpt.isEmpty()) continue;

            GearItem gearItem = gearItemOpt.get();
            if (gearItem.getItemInfo().fixedStats().majorIds().stream()
                    .anyMatch(majorId -> AUTO_CASTER_MAJOR_IDS.contains(majorId.name()))) {
                return true;
            }
        }

        return Models.Raid.getRaidMajorIds(McUtils.mc().getUser().getName()).stream()
                .anyMatch(AUTO_CASTER_MAJOR_IDS::contains);
    }

    /**
     * @return The number of items in the player's inventory with the given name
     */
    public int getAmountInInventory(String name) {
        int amount = 0;

        for (ItemStack itemStack : McUtils.inventory().items) {
            StyledText itemName = StyledText.fromComponent(itemStack.getHoverName())
                    .getNormalized()
                    .trim();
            if (itemName.getString().endsWith(name)) {
                amount += itemStack.getCount();
            }
        }

        return amount;
    }

    public int getIngredientAmountInInventory(String name) {
        return McUtils.inventory().items.stream()
                .filter(itemStack -> {
                    Optional<IngredientItem> ingredientItem = Models.Item.asWynnItem(itemStack, IngredientItem.class);
                    if (ingredientItem.isEmpty()) return false;
                    return ingredientItem.get().getName().startsWith(name);
                })
                .mapToInt(itemStack -> itemStack.count)
                .sum();
    }

    public int getMaterialsAmountInInventory(String name, int tier, boolean exact) {
        return McUtils.inventory().items.stream()
                .filter(itemStack -> {
                    Optional<MaterialItem> materialItemOpt = Models.Item.asWynnItem(itemStack, MaterialItem.class);
                    if (materialItemOpt.isEmpty()) return false;
                    MaterialItem materialItem = materialItemOpt.get();
                    if (!itemStack.getHoverName().getString().startsWith(name)) return false;
                    return exact ? materialItem.getQualityTier() == tier : materialItem.getQualityTier() >= tier;
                })
                .mapToInt(itemStack -> itemStack.count)
                .sum();
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

    private void updateCache() {
        Inventory inventory = McUtils.inventory();

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

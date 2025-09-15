/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemrecord;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.WynnItem;
import com.wynntils.services.itemrecord.type.SavedItem;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemRecordService extends Service {
    private static final String DEFAULT_CATEGORY = "Uncategorized";

    @Persisted
    public final Storage<Set<SavedItem>> savedItems = new Storage<>(new TreeSet<>());

    // This is basically a trash can for items that can't be decoded
    // This is useful for a backup in case the item can be decoded in the future (with a new version of the mod)
    @Persisted
    private final Storage<Set<SavedItem>> faultyItems = new Storage<>(new TreeSet<>());

    @Persisted
    public final Storage<Set<String>> categories = new Storage<>(new TreeSet<>(List.of(DEFAULT_CATEGORY)));

    public ItemRecordService() {
        super(List.of());
    }

    public boolean saveItem(WynnItem wynnItem, ItemStack itemStack, Component itemName) {
        // Regular ItemStack can't be converted to json so store the tags needed
        // to recreate it
        SavedItem itemToSave =
                SavedItem.create(wynnItem, new TreeSet<>(List.of(Services.ItemRecord.getDefaultCategory())), itemStack);

        // Check if the item is already saved
        if (savedItems.get().contains(itemToSave)) {
            McUtils.sendMessageToClient(Component.translatable(
                            "screens.wynntils.itemSharing.alreadySaved",
                            StyledText.fromComponent(itemName).getString() + ChatFormatting.RED)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        savedItems.get().add(itemToSave);

        Services.ItemRecord.savedItems.touched();

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.itemSharing.savedToRecord",
                        StyledText.fromComponent(itemName).getString() + ChatFormatting.GREEN)
                .withStyle(ChatFormatting.GREEN));

        return true;
    }

    public void moveSelectedItems(List<Pair<String, String>> selectedItems, String category, boolean keepOriginal) {
        for (Pair<String, String> selectedItem : selectedItems) {
            SavedItem savedItem = Services.ItemRecord.getItem(selectedItem.b());
            moveItemCategory(savedItem, category, selectedItem.a(), keepOriginal);
        }
    }

    public void moveItemCategory(
            SavedItem savedItem, String currentCategory, String originalCategory, boolean keepOriginal) {
        savedItem.categories().add(currentCategory);

        if (!keepOriginal) {
            savedItem.categories().remove(originalCategory);
        }

        Services.ItemRecord.savedItems.touched();
    }

    public void deleteItem(String base64) {
        for (SavedItem savedItem : savedItems.get()) {
            if (savedItem.base64().equals(base64)) {
                Services.ItemRecord.savedItems.get().remove(savedItem);
                Services.ItemRecord.savedItems.touched();
                break;
            }
        }
    }

    public void addCategory(String newCategory, List<Pair<String, String>> selectedItems, boolean keepOriginals) {
        // Save new category
        Services.ItemRecord.categories.get().add(newCategory);
        Services.ItemRecord.categories.touched();

        if (!selectedItems.isEmpty()) {
            moveSelectedItems(selectedItems, newCategory, keepOriginals);
        }
    }

    public void renameCategory(String originalName, String newName) {
        // Add renamed category and remove previous name
        categories.get().add(newName);
        categories.get().remove(originalName);
        categories.touched();

        for (SavedItem savedItem : savedItems.get()) {
            // If an item is in the current category, add it to the renamed and remove previous name
            if (savedItem.categories().contains(originalName)) {
                savedItem.categories().add(newName);
                savedItem.categories().remove(originalName);
            }
        }

        savedItems.touched();
    }

    public void deleteCategory(String categoryToDelete) {
        if (KeyboardUtils.isShiftDown()) {
            Set<SavedItem> newSavedItems = new TreeSet<>();

            // Remove category from all items
            for (SavedItem savedItem : savedItems.get()) {
                savedItem.categories().remove(categoryToDelete);

                // If the item is no longer in any categories then it should be deleted
                if (!savedItem.categories().isEmpty()) {
                    newSavedItems.add(savedItem);
                }
            }

            Services.ItemRecord.savedItems.store(newSavedItems);
            Services.ItemRecord.savedItems.touched();
        } else if (!categoryToDelete.equals(Services.ItemRecord.getDefaultCategory())) {
            // Remove category from all items and add default
            for (SavedItem savedItem : savedItems.get()) {
                savedItem.categories().remove(categoryToDelete);
                savedItem.categories().add(Services.ItemRecord.getDefaultCategory());
            }

            Services.ItemRecord.savedItems.store(savedItems.get());
            Services.ItemRecord.savedItems.touched();
        }

        // If current category is not the default, delete it
        if (!categoryToDelete.equals(Services.ItemRecord.getDefaultCategory())) {
            Services.ItemRecord.categories.get().remove(categoryToDelete);
            Services.ItemRecord.categories.touched();
        }
    }

    public String getDefaultCategory() {
        return DEFAULT_CATEGORY;
    }

    public void cleanupItemRecord() {
        // Try to remove all invalid items
        List<SavedItem> itemsToRemove = new ArrayList<>();
        for (SavedItem savedItem : savedItems.get()) {
            try {
                savedItem.wynnItem();
            } catch (Exception e) {
                WynntilsMod.warn("Removing invalid item from item record: " + savedItem.base64(), e);
                itemsToRemove.add(savedItem);
                faultyItems.get().add(savedItem);
            }
        }

        // Check if the mod can decode faulty items
        List<SavedItem> itemsToReadd = new ArrayList<>();
        for (SavedItem faultyItem : faultyItems.get()) {
            try {
                faultyItem.wynnItem();
                itemsToReadd.add(faultyItem);
            } catch (Exception e) {
                // continue, we still can't decode this item
            }
        }

        // If there is nothing to do, return
        if (itemsToRemove.isEmpty() && itemsToReadd.isEmpty()) return;

        // Remove invalid items
        savedItems.get().removeAll(itemsToRemove);
        faultyItems.get().addAll(itemsToRemove);

        // Readd items that can now be decoded
        faultyItems.get().removeAll(itemsToReadd);
        savedItems.get().addAll(itemsToReadd);

        // Save changes
        faultyItems.touched();
        savedItems.touched();

        WynntilsMod.warn("Item record cleanup complete. Removed " + itemsToRemove.size() + " invalid items. Readded "
                + itemsToReadd.size() + " items that can now be decoded.");
        McUtils.sendMessageToClient(Component.translatable(
                        "service.wynntils.itemRecord.cleanupComplete", itemsToRemove.size(), itemsToReadd.size())
                .withStyle(ChatFormatting.YELLOW));
    }

    private SavedItem getItem(String base64) {
        for (SavedItem savedItem : savedItems.get()) {
            if (savedItem.base64().equals(base64)) {
                return savedItem;
            }
        }

        return null;
    }
}

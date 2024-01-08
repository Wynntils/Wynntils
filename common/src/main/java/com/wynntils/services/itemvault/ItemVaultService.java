/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemvault;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.items.WynnItem;
import com.wynntils.services.itemvault.type.SavedItem;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemVaultService extends Service {
    private static final String DEFAULT_CATEGORY = "Uncategorized";

    @Persisted
    public final Storage<Set<SavedItem>> savedItems = new Storage<>(new TreeSet<>());

    @Persisted
    public final Storage<Set<String>> categories = new Storage<>(new TreeSet<>(List.of(DEFAULT_CATEGORY)));

    public ItemVaultService() {
        super(List.of());
    }

    public boolean saveItem(WynnItem wynnItem, ItemStack itemStack, Component itemName) {
        // Regular ItemStack can't be converted to json so store the tags needed
        // to recreate it
        SavedItem itemToSave =
                SavedItem.create(wynnItem, new TreeSet<>(List.of(Services.ItemVault.getDefaultCategory())), itemStack);

        // Check if the item is already saved
        if (savedItems.get().contains(itemToSave)) {
            McUtils.sendMessageToClient(Component.translatable("screens.wynntils.itemSharing.alreadySaved", itemName)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        savedItems.get().add(itemToSave);

        Services.ItemVault.savedItems.touched();

        McUtils.sendMessageToClient(Component.translatable("screens.wynntils.itemSharing.savedToVault", itemName)
                .withStyle(ChatFormatting.GREEN));

        return true;
    }

    public void moveSelectedItems(List<Pair<String, String>> selectedItems, String category, boolean keepOriginal) {
        for (Pair<String, String> selectedItem : selectedItems) {
            SavedItem savedItem = Services.ItemVault.getItem(selectedItem.b());

            if (selectedItem != null) {
                moveItemCategory(savedItem, category, selectedItem.a(), keepOriginal);
            }
        }
    }

    public void moveItemCategory(
            SavedItem savedItem, String currentCategory, String originalCategory, boolean keepOriginal) {
        savedItem.categories().add(currentCategory);

        if (!keepOriginal) {
            savedItem.categories().remove(originalCategory);
        }

        Services.ItemVault.savedItems.touched();
    }

    public void deleteItem(String base64) {
        for (SavedItem savedItem : savedItems.get()) {
            if (savedItem.base64().equals(base64)) {
                Services.ItemVault.savedItems.get().remove(savedItem);
                Services.ItemVault.savedItems.touched();
                break;
            }
        }
    }

    public void addCategory(String newCategory, List<Pair<String, String>> selectedItems, boolean keepOriginals) {
        // Save new category
        Services.ItemVault.categories.get().add(newCategory);
        Services.ItemVault.categories.touched();

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

            Services.ItemVault.savedItems.store(newSavedItems);
            Services.ItemVault.savedItems.touched();
        } else if (!categoryToDelete.equals(Services.ItemVault.getDefaultCategory())) {
            // Remove category from all items and add default
            for (SavedItem savedItem : savedItems.get()) {
                savedItem.categories().remove(categoryToDelete);
                savedItem.categories().add(Services.ItemVault.getDefaultCategory());
            }

            Services.ItemVault.savedItems.store(savedItems.get());
            Services.ItemVault.savedItems.touched();
        }

        // If current category is not the default, delete it
        if (!categoryToDelete.equals(Services.ItemVault.getDefaultCategory())) {
            Services.ItemVault.categories.get().remove(categoryToDelete);
            Services.ItemVault.categories.touched();
        }
    }

    public String getDefaultCategory() {
        return DEFAULT_CATEGORY;
    }

    public SavedItem getItem(String base64) {
        for (SavedItem savedItem : savedItems.get()) {
            if (savedItem.base64().equals(base64)) {
                return savedItem;
            }
        }

        return null;
    }
}

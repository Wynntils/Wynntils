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
import com.wynntils.utils.mc.McUtils;
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

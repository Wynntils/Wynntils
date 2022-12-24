/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.IngredientItemStack;
import com.wynntils.wynn.model.ItemProfilesManager;
import java.util.List;

public class ItemManager extends Manager {

    private List<GearItemStack> allGearItems = List.of();
    private List<IngredientItemStack> allIngredientItems = List.of();

    public ItemManager(ItemProfilesManager itemProfilesManager) {
        super(List.of(itemProfilesManager));
    }

    public List<IngredientItemStack> getAllIngredientItems() {
        if (allIngredientItems.isEmpty()) {
            allIngredientItems = Managers.ItemProfiles.getIngredientsCollection().stream()
                    .map(IngredientItemStack::new)
                    .toList();
        }

        return allIngredientItems;
    }

    public List<GearItemStack> getAllGearItems() {
        if (allGearItems.isEmpty()) {
            // Populate list
            allGearItems = Managers.ItemProfiles.getItemsCollection().stream()
                    .map(GearItemStack::new)
                    .toList();
        }

        return allGearItems;
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.Managers;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.model.ItemProfilesManager;
import java.util.List;

public class GearItemManager extends Manager {

    private List<GearItemStack> allGearItems = List.of();

    public GearItemManager(ItemProfilesManager itemProfilesManager) {
        super(List.of(itemProfilesManager));
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

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Models;
import com.wynntils.wynn.model.ItemProfilesManager;
import java.util.List;

public class ItemManager extends Manager {
    public ItemManager(ItemProfilesManager itemProfilesManager) {
        super(List.of(itemProfilesManager));

        // This is slightly hacky, awaiting the full refactoring
        WynntilsMod.registerEventListener(Models.Item);
        Models.Item.init();
    }
}

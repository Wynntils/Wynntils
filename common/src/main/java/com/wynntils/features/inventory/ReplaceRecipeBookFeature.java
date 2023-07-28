/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.RecipeBookOpenEvent;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ReplaceRecipeBookFeature extends Feature {
    @SubscribeEvent
    public void onRecipeBookOpen(RecipeBookOpenEvent event) {
        event.setCanceled(true);

        WynntilsMenuScreenBase.openBook(WynntilsGuidesListScreen.create());
    }
}

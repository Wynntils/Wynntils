/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.RecipeBookOpenEvent;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ReplaceRecipeBookFeature extends UserFeature {
    @SubscribeEvent
    public void onRecipeBookOpen(RecipeBookOpenEvent event) {
        event.setCanceled(true);

        McUtils.mc().setScreen(WynntilsGuidesListScreen.create(McUtils.mc().screen));
    }
}

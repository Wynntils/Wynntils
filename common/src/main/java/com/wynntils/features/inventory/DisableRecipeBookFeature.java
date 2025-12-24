/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RecipeBookButtonCreateEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Disables the recipe book as the Wynncraft texture pack hides it, but has no way to disable it.
 */
@ConfigCategory(Category.INVENTORY)
public class DisableRecipeBookFeature extends Feature {
    public DisableRecipeBookFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onRecipeBookOpen(RecipeBookButtonCreateEvent event) {
        event.setCanceled(true);
    }
}

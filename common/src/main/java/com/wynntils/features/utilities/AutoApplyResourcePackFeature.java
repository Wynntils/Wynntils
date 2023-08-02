/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ResourcePackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class AutoApplyResourcePackFeature extends Feature {
    @Override
    public void onDisable() {
        Services.ResourcePack.setRequestedPreloadHash("");
    }

    @SubscribeEvent
    public void onResourcePackLoad(ResourcePackEvent event) {
        String packHash = Services.ResourcePack.calculateHash(event.getUrl());

        String currentHash = Services.ResourcePack.getRequestedPreloadHash();
        if (!packHash.equals(currentHash)) {
            // Use this resource pack as our preloaded pack
            Services.ResourcePack.setRequestedPreloadHash(packHash);
        }
    }
}

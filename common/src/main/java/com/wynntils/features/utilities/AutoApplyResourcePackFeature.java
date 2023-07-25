/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.ResourcePackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class AutoApplyResourcePackFeature extends Feature {
    @Override
    public void onDisable() {
        Managers.ResourcePack.setRequestedPreloadHash("");
    }

    @SubscribeEvent
    public void onResourcePackLoad(ResourcePackEvent event) {
        String packHash = Managers.ResourcePack.calculateHash(event.getUrl());

        String currentHash = Managers.ResourcePack.getRequestedPreloadHash();
        if (!packHash.equals(currentHash)) {
            // Use this resource pack as our preloaded pack
            Managers.ResourcePack.setRequestedPreloadHash(packHash);
        }
    }
}

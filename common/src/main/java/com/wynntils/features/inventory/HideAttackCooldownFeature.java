/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ItemCooldownRenderEvent;
import com.wynntils.utils.wynn.ItemUtils;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class HideAttackCooldownFeature extends Feature {
    public HideAttackCooldownFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onCooldownRender(ItemCooldownRenderEvent event) {
        if (ItemUtils.isWeapon(event.getItemStack())) return;

        event.setCanceled(true);
    }
}

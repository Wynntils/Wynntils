/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class TooltipVanillaHideFeature extends Feature {
    @Persisted
    private final Config<Boolean> hideAdvanced = new Config<>(true);

    public TooltipVanillaHideFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onTooltipFlagsAdvanced(ItemTooltipFlagsEvent event) {
        if (!hideAdvanced.get()) return;

        event.setFlags(TooltipFlag.NORMAL);
    }
}

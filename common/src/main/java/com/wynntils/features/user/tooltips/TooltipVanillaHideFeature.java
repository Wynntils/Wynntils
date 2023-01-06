/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipFlags;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class TooltipVanillaHideFeature extends UserFeature {
    @Config
    public boolean hideAdvanced = true;

    @Config
    public boolean hideAdditionalnfo = true;

    @SubscribeEvent
    public void onTooltipFlagsAdvanced(ItemTooltipFlags.Advanced event) {
        if (!hideAdvanced) return;

        event.setFlags(TooltipFlag.NORMAL);
    }

    @SubscribeEvent
    public void onTooltipFlagsMask(ItemTooltipFlags.Mask event) {
        if (!hideAdditionalnfo) return;

        event.setMask(0);
    }
}

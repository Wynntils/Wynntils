/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.combat;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.ArmSwingEvent;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This feature fixes an issue where a Archers holding bows may start
 * casting spells just by dropping items from the inventory screen.
 */
@FeatureInfo(category = FeatureCategory.COMBAT)
public class FixCastingSpellsFromInventoryFeature extends UserFeature {

    @SubscribeEvent
    public void onSetSlot(ArmSwingEvent event) {
        if (event.getActionContext() == ArmSwingEvent.ArmSwingContext.DROP_ITEM_FROM_INVENTORY_SCREEN
                && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
        }
    }
}

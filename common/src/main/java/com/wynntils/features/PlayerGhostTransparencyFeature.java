/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.wc.utils.WynnPlayerUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerGhostTransparencyFeature extends Feature {

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (e.getEntity() instanceof Player player) {
            // TODO make this variable a setting
            if (WynnPlayerUtils.isPlayerGhost(player)) {
                e.setTranslucence(0.75f);
            }
        }
    }
}

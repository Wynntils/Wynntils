/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.wc.utils.WynnPlayerUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerGhostTransparencyFeature extends FeatureBase {
    public PlayerGhostTransparencyFeature() {
        setupEventListener();
    }

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        // TODO make this variable a setting
        if (WynnPlayerUtils.isPlayerGhost(player)) {
            e.setTranslucence(0.75f);
        }
    }
}

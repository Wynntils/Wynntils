/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.GameplayImpact;
import com.wynntils.core.features.properties.FeatureInfo.PerformanceImpact;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.wc.utils.WynnPlayerUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.SMALL, performance = PerformanceImpact.SMALL)
public class PlayerGhostTransparencyFeature extends UserFeature {
    public static PlayerGhostTransparencyFeature INSTANCE;

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

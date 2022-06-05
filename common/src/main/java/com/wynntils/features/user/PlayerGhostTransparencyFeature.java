/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.ConfigurableInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.wc.utils.WynnPlayerUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(stability = Stability.STABLE)
@ConfigurableInfo(category = "Utilities")
public class PlayerGhostTransparencyFeature extends UserFeature {

    @ConfigOption(
            displayName = "Player Ghost Translucence",
            description = "The level of translucence that should be applied to player ghosts")
    public static float playerGhostTranslucenceLevel = 0.75f;

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        if (WynnPlayerUtils.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel);
        }
    }
}

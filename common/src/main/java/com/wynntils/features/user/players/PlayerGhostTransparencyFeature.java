/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.players;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.LivingEntityArmorTranslucenceEvent;
import com.wynntils.mc.event.LivingEntityRenderTranslucenceEvent;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.PLAYERS)
public class PlayerGhostTransparencyFeature extends UserFeature {
    @Config
    public float playerGhostTranslucenceLevel = 0.75f;

    @Config
    public boolean transparentGhostArmor = true;

    @SubscribeEvent
    public void onGhostArmorRender(LivingEntityArmorTranslucenceEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        if (WynnPlayerUtils.isPlayerGhost(player) && transparentGhostArmor) {
            e.setTranslucence(playerGhostTranslucenceLevel);
        }
    }

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucenceEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        if (WynnPlayerUtils.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel);
        }
    }
}

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
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.mc.event.PlayerArmorRenderEvent;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.PLAYERS)
public class PlayerGhostTransparencyFeature extends UserFeature {
    @Config
    public float playerGhostTranslucenceLevel = 0.75f;

    @Config
    public boolean transparentPlayerGhostArmor = false;

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (WynnPlayerUtils.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel);
        }
    }

    @SubscribeEvent
    public void onPlayerArmorRender(PlayerArmorRenderEvent event) {
        if (!transparentPlayerGhostArmor) return;

        if (WynnPlayerUtils.isPlayerGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}

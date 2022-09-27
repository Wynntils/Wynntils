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
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.PLAYERS)
public class PlayerTransparencyFeature extends UserFeature {
    @Config
    public static float playerTranslucenceLevel = 0.75f;

    @Config
    public static boolean playerGhostTranslucent = true;

    @Config
    public static boolean playerTranslucent = true;

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        boolean transparent = isTransparent(e, player);

        if (transparent) {
            e.setTranslucence(playerTranslucenceLevel);
        }
    }

    private static boolean isTransparent(LivingEntityRenderTranslucentCheckEvent e, Player player) {
        if (playerGhostTranslucent && WynnPlayerUtils.isPlayerGhost(player)) {
            return true;
        }

        return playerTranslucent
                && e.getEntity().is(McUtils.player())
                && e.getEntity().isInvisible();
    }
}

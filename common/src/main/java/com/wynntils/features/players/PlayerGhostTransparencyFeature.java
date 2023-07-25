/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.PLAYERS)
public class PlayerGhostTransparencyFeature extends Feature {
    @RegisterConfig
    public final Config<Float> playerGhostTranslucenceLevel = new Config<>(0.75f);

    @RegisterConfig
    public final Config<Boolean> transparentPlayerGhostArmor = new Config<>(true);

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (Models.Player.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel.get());
        }
    }

    @SubscribeEvent
    public void onPlayerArmorRender(PlayerRenderLayerEvent.Armor event) {
        if (!transparentPlayerGhostArmor.get()) return;

        if (Models.Player.isPlayerGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}

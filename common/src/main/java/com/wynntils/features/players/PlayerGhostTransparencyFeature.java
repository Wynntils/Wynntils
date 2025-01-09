/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.PLAYERS)
public class PlayerGhostTransparencyFeature extends Feature {
    @Persisted
    public final Config<Float> playerGhostTranslucenceLevel = new Config<>(0.75f);

    @Persisted
    public final Config<Boolean> transparentPlayerGhostArmor = new Config<>(true);

    @SubscribeEvent
    public void onTranslucentCheck(RenderTranslucentCheckEvent.Body e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (Models.Player.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel.get());
        }
    }

    @SubscribeEvent
    public void onTranslucentCheckForCape(RenderTranslucentCheckEvent.Cape e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (Models.Player.isPlayerGhost(player)) {
            e.setTranslucence(playerGhostTranslucenceLevel.get());
        }
    }

    @SubscribeEvent
    public void onPlayerArmorRender(PlayerRenderLayerEvent.Armor event) {
        if (!transparentPlayerGhostArmor.get()) return;
        Entity entity = ((EntityRenderStateExtension) event.getPlayerRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;

        if (Models.Player.isPlayerGhost(player)) {
            event.setCanceled(true);
        }
    }
}

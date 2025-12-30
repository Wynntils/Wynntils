/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class WynntilsCosmeticsFeature extends Feature {
    @Persisted
    public final Config<Boolean> renderOwnCape = new Config<>(true);

    public WynntilsCosmeticsFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onCapeRender(PlayerRenderLayerEvent.Cape event) {
        if (!isEnabled() || !Managers.Connection.onServer()) return;

        Entity entity = ((EntityRenderStateExtension) event.getPlayerRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (McUtils.player().is(player) && !renderOwnCape.get()) return;

        if (Services.Cosmetics.shouldRenderCape(player, false)) {
            // Cancel default cape rendering, so ours doesn't cause a double up of capes
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onElytraRender(PlayerRenderLayerEvent.Elytra event) {
        if (!isEnabled() || !Managers.Connection.onServer()) return;

        Entity entity = ((EntityRenderStateExtension) event.getPlayerRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (McUtils.player().is(player) && !renderOwnCape.get()) return;

        if (Services.Cosmetics.shouldRenderCape(player, true)) {
            // This might not be necessary?
            event.setCanceled(true);
        }
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.cosmetics;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.cosmetics.type.WynntilsCapeLayer;
import com.wynntils.models.cosmetics.type.WynntilsElytraLayer;
import com.wynntils.models.cosmetics.type.WynntilsLayer;
import com.wynntils.models.players.type.CosmeticInfo;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class CosmeticsModel extends Model {
    private static final BiFunction<
                    RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>,
                    EntityModelSet,
                    WynntilsLayer>
            CAPE_LAYER = (playerRenderer, playerModel) -> new WynntilsCapeLayer(playerRenderer);
    private static final BiFunction<
                    RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>,
                    EntityModelSet,
                    WynntilsLayer>
            ELYTRA_LAYER = (playerRenderer, playerModel) -> new WynntilsElytraLayer(playerRenderer, playerModel);

    private static final List<
                    BiFunction<
                            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>,
                            EntityModelSet,
                            WynntilsLayer>>
            REGISTERED_LAYERS = List.of(CAPE_LAYER, ELYTRA_LAYER);

    public CosmeticsModel() {
        super(List.of());
    }

    // Note: This method is static deliberately. We don't want to reference Models in the mixin this is called, due to
    // it's unpredictable class loading.
    public static List<
                    BiFunction<
                            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>,
                            EntityModelSet,
                            WynntilsLayer>>
            getRegisteredLayers() {
        return REGISTERED_LAYERS;
    }

    public boolean shouldRenderCape(Player player, boolean elytra) {
        if (player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE)) return false;

        if (Models.Player.getUser(player.getUUID()) == null
                || Models.Player.getUserCosmeticTexture(player.getUUID()) == null) return false;

        CosmeticInfo cosmetics = Models.Player.getUser(player.getUUID()).cosmetics();
        return (elytra ? cosmetics.hasElytra() : cosmetics.hasCape());
    }

    // TODO: implement ear rendering
    public boolean shouldRenderEars(AbstractClientPlayer player) {
        if (!player.isSkinLoaded() || player.isInvisible()) return false;

        if (Models.Player.getUser(player.getUUID()) == null) return false;

        return Models.Player.getUser(player.getUUID()).cosmetics().hasEars();
    }

    public ResourceLocation getCapeTexture(Player player) {
        ResourceLocation[] textures = Models.Player.getUserCosmeticTexture(player.getUUID());
        if (textures == null) return null;

        int frames = textures.length;
        if (frames == 1) return textures[0];

        // This makes animated capes cycle through all their frames in 2 seconds,
        // regardless of how many frames of animation are present. Could be changed to an account setting?
        double percentage = ((System.currentTimeMillis() % 2000) / 2000d);
        int currentFrame = (int) (frames * percentage);
        return textures[currentFrame];
    }
}

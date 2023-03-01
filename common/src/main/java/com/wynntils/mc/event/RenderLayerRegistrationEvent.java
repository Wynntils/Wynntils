/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.eventbus.api.Event;

public class RenderLayerRegistrationEvent extends Event {
    private final PlayerRenderer playerRenderer;
    private final EntityRendererProvider.Context context;
    private final boolean slim;
    private final List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layers;

    public RenderLayerRegistrationEvent(
            PlayerRenderer playerRenderer, EntityRendererProvider.Context context, boolean slim) {
        this.playerRenderer = playerRenderer;
        this.context = context;
        this.slim = slim;
        this.layers = new ArrayList<>();
    }

    public void registerLayer(RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layer) {
        layers.add(layer);
    }

    public PlayerRenderer getPlayerRenderer() {
        return playerRenderer;
    }

    public EntityRendererProvider.Context getContext() {
        return context;
    }

    public boolean isSlimPlayerRenderer() {
        return slim;
    }

    public List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> getRegisteredLayers() {
        return Collections.unmodifiableList(layers);
    }
}

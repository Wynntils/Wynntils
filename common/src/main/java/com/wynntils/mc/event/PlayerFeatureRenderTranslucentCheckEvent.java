/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.mc.extension.PlayerModelExtension;
import net.minecraft.world.entity.LivingEntity;

public abstract class PlayerFeatureRenderTranslucentCheckEvent extends LivingEntityRenderTranslucentCheckEvent {
    private final PlayerModelExtension playerModelExtension;

    protected PlayerFeatureRenderTranslucentCheckEvent(
            boolean translucent, LivingEntity entity, float translucence, PlayerModelExtension playerModelExtension) {
        super(translucent, entity, translucence);
        this.playerModelExtension = playerModelExtension;
    }

    public PlayerModelExtension getPlayerModelExtension() {
        return this.playerModelExtension;
    }

    /**
     * Fired when {@link net.minecraft.client.renderer.entity.layers.CapeLayer} checks whether a living
     * entity cape should be rendered translucent or not
     */
    public static class Cape extends PlayerFeatureRenderTranslucentCheckEvent {
        public Cape(
                boolean translucent,
                LivingEntity entity,
                float translucence,
                PlayerModelExtension playerModelExtension) {
            super(translucent, entity, translucence, playerModelExtension);
        }

        /**
         * Translucence value needs to pass into {@link net.minecraft.client.model.PlayerModel} class,
         * because {@link net.minecraft.client.model.PlayerModel#renderCloak(PoseStack, VertexConsumer, int, int)} method does not accept {@code color}
         * argument
         * @param translucence
         */
        @Override
        public void setTranslucence(float translucence) {
            super.setTranslucence(translucence);
            this.getPlayerModelExtension().setTranslucenceCape(translucence);
        }
    }
}

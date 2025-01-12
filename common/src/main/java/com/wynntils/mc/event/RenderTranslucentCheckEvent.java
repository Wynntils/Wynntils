/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.bus.api.Event;

public abstract class RenderTranslucentCheckEvent extends Event {
    private boolean translucent;
    private final LivingEntityRenderState entityRenderState;
    private float translucence;

    protected RenderTranslucentCheckEvent(
            boolean translucent, LivingEntityRenderState entityRenderState, float translucence) {
        this.translucent = translucent;
        this.entityRenderState = entityRenderState;
        this.translucence = translucence;
    }

    public LivingEntityRenderState getEntityRenderState() {
        return entityRenderState;
    }

    public float getTranslucence() {
        return translucence;
    }

    public void setTranslucence(float translucence) {
        this.translucence = translucence;
        this.translucent = translucence < 1.0f;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    /**
     * Fired when {@link net.minecraft.client.renderer.entity.LivingEntityRenderer} checks whether an
     * entity should be rendered translucent or not
     */
    public static class Body extends RenderTranslucentCheckEvent {
        public Body(boolean translucent, LivingEntityRenderState entityRenderState, float translucence) {
            super(translucent, entityRenderState, translucence);
        }
    }

    /**
     * Fired when {@link net.minecraft.client.renderer.entity.layers.CapeLayer} checks whether a living
     * entity cape should be rendered translucent or not
     */
    public static class Cape extends RenderTranslucentCheckEvent {
        public Cape(boolean translucent, LivingEntityRenderState entityRenderState, float translucence) {
            super(translucent, entityRenderState, translucence);
        }
    }
}

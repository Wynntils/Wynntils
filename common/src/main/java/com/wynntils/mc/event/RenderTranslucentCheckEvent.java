/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public abstract class RenderTranslucentCheckEvent extends Event {
    private boolean translucent;
    private final LivingEntity entity;
    private float translucence;

    public RenderTranslucentCheckEvent(boolean translucent, LivingEntity entity, float translucence) {
        this.translucent = translucent;
        this.entity = entity;
        this.translucence = translucence;
    }

    public LivingEntity getEntity() {
        return entity;
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
        public Body(boolean translucent, LivingEntity entity, float translucence) {
            super(translucent, entity, translucence);
        }
    }

    /**
     * Fired when {@link net.minecraft.client.renderer.entity.layers.CapeLayer} checks whether a living
     * entity cape should be rendered translucent or not
     */
    public static class Cape extends RenderTranslucentCheckEvent {
        public Cape(boolean translucent, LivingEntity entity, float translucence) {
            super(translucent, entity, translucence);
        }
    }
}

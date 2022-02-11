/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when {@link net.minecraft.client.renderer.entity.LivingEntityRenderer} checks whether an
 * entity should be rendered translucent or not
 */
public class LivingEntityRenderTranslucentCheckEvent extends Event {
    private boolean translucent;
    private final LivingEntity entity;
    private float translucense;

    public LivingEntityRenderTranslucentCheckEvent(
            boolean translucent, LivingEntity entity, float translucense) {
        this.translucent = translucent;
        this.entity = entity;
        this.translucense = translucense;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getTranslucense() {
        return translucense;
    }

    public void setTranslucense(float translucense) {
        this.translucense = translucense;
        this.translucent = translucense == 1f;
    }

    public boolean isTranslucent() {
        return translucent;
    }
}

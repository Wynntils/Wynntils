/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class LivingEntityArmorTranslucenceEvent extends Event {

    private final LivingEntity entity;
    private float translucence;

    public LivingEntityArmorTranslucenceEvent(LivingEntity entity) {
        this.entity = entity;
        this.translucence = 1.0f;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getTranslucence() {
        return translucence;
    }

    public void setTranslucence(float translucence) {
        this.translucence = translucence;
    }
}

/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;

public class LivingEvent extends EntityEvent {
    private final LivingEntity entityLiving;

    public LivingEvent(LivingEntity entity) {
        super(entity);
        this.entityLiving = entity;
    }

    public LivingEntity getEntityLiving() {
        return this.entityLiving;
    }
}

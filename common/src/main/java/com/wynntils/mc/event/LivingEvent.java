/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;

public abstract class LivingEvent extends EntityEvent {
    private final LivingEntity entityLiving;

    protected LivingEvent(LivingEntity entity) {
        super(entity);
        this.entityLiving = entity;
    }

    public LivingEntity getEntityLiving() {
        return this.entityLiving;
    }
}

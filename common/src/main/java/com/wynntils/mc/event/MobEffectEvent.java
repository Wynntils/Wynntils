/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public abstract class MobEffectEvent extends Event {
    private final Entity entity;
    private final MobEffect effect;

    protected MobEffectEvent(Entity entity, MobEffect effect) {
        this.entity = entity;
        this.effect = effect;
    }

    public Entity getEntity() {
        return entity;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public static final class Update extends MobEffectEvent {
        private final byte effectAmplifier;
        private final int effectDurationTicks;

        public Update(Entity entity, MobEffect effect, byte effectAmplifier, int effectDurationTicks) {
            super(entity, effect);
            this.effectAmplifier = effectAmplifier;
            this.effectDurationTicks = effectDurationTicks;
        }

        public byte getEffectAmplifier() {
            return effectAmplifier;
        }

        public int getEffectDurationTicks() {
            return effectDurationTicks;
        }
    }

    public static final class Remove extends MobEffectEvent {
        public Remove(Entity entity, MobEffect effect) {
            super(entity, effect);
        }
    }
}

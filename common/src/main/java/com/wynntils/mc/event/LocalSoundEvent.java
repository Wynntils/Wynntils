/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public abstract class LocalSoundEvent extends BaseEvent {
    private final SoundEvent sound;

    protected LocalSoundEvent(SoundEvent sound) {
        this.sound = sound;
    }

    public SoundEvent getSound() {
        return sound;
    }

    public static final class Client extends LocalSoundEvent implements CancelRequestable {
        private final SoundSource source;

        public Client(SoundEvent sound, SoundSource source) {
            super(sound);
            this.source = source;
        }

        public SoundSource getSource() {
            return source;
        }
    }

    public static final class Player extends LocalSoundEvent implements CancelRequestable {
        public Player(SoundEvent sound) {
            super(sound);
        }
    }

    public static final class LocalEntity extends LocalSoundEvent implements CancelRequestable {
        private final Entity entity;

        public LocalEntity(SoundEvent sound, Entity entity) {
            super(sound);
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.particle.type;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public enum ParticleType {
    LOOTRUN_TASK(ParticleTypes.FIREWORK);

    private final ParticleOptions compatibleParticleEffect;

    ParticleType(ParticleOptions compatibleParticleEffect) {
        this.compatibleParticleEffect = compatibleParticleEffect;
    }

    public ParticleOptions getCompatibleParticleEffect() {
        return compatibleParticleEffect;
    }
}

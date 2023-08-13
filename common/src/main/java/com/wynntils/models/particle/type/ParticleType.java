/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.particle.type;

import com.wynntils.models.particle.verifiers.LootrunTaskParticleVerifier;
import com.wynntils.models.particle.verifiers.ParticleVerifier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public enum ParticleType {
    LOOTRUN_TASK(new LootrunTaskParticleVerifier(), ParticleTypes.FIREWORK);

    private final ParticleVerifier particleVerifier;
    private final ParticleOptions compatibleParticleEffect;

    ParticleType(ParticleVerifier particleVerifier, ParticleOptions compatibleParticleEffect) {
        this.particleVerifier = particleVerifier;
        this.compatibleParticleEffect = compatibleParticleEffect;
    }

    public ParticleVerifier getParticleVerifier() {
        return particleVerifier;
    }

    public ParticleOptions getCompatibleParticleEffect() {
        return compatibleParticleEffect;
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.particle.type;

import com.wynntils.models.particle.verifiers.ParticleVerifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;

public class UnverifiedParticle {
    private final ParticleType particleType;
    private final ParticleOptions particleEffect;
    private final List<Position> particles = new ArrayList<>();

    public UnverifiedParticle(ParticleType particleType, ParticleOptions particleOptions) {
        this.particleType = particleType;
        this.particleEffect = particleOptions;
    }

    public boolean addNewParticle(Position newPosition, ParticleOptions newParticleOption) {
        if (newParticleOption != particleEffect) return false;

        if (particleType.getParticleVerifier().verifyNewPosition(particles, newPosition)) {
            particles.add(newPosition);
            return true;
        }

        return false;
    }

    public ParticleVerifier.VerificationResult verifyCompleteness() {
        return particleType.getParticleVerifier().verifyCompleteness(particles);
    }

    public ParticleOptions getParticleEffect() {
        return particleEffect;
    }

    public Particle getParticle() {
        return particleType.getParticleVerifier().getParticle(particles);
    }
}

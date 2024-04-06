/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.particle.type;

import com.wynntils.core.components.Handlers;
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

        if (Handlers.Particle.getParticleVerifier(particleType).verifyNewPosition(particles, newPosition)) {
            particles.add(newPosition);
            return true;
        }

        return false;
    }

    public ParticleVerifier.VerificationResult verifyCompleteness() {
        return Handlers.Particle.getParticleVerifier(particleType).verifyCompleteness(particles);
    }

    public ParticleOptions getParticleEffect() {
        return particleEffect;
    }

    public Particle getParticle() {
        return Handlers.Particle.getParticleVerifier(particleType).getParticle(particles);
    }
}

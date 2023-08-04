/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.particle;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ParticleAddedEvent;
import com.wynntils.models.particle.event.ParticleVerifiedEvent;
import com.wynntils.models.particle.type.Particle;
import com.wynntils.models.particle.type.ParticleType;
import com.wynntils.models.particle.type.UnverifiedParticle;
import com.wynntils.models.particle.verifiers.ParticleVerifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ParticleModel extends Model {
    private Set<UnverifiedParticle> unverifiedParticles = new HashSet<>();

    public ParticleModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onParticleAdded(ParticleAddedEvent event) {
        Position particlePosition = new Vec3(event.getX(), event.getY(), event.getZ());

        // We depend on the fact that particle groups are sent in order:
        // If we have A and B particle animation, then we will receive all A particles, then all B particles
        if (unverifiedParticles.isEmpty()) {
            for (ParticleType particleType : ParticleType.values()) {
                // Check if the this type is compatible with the particle effect type
                if (!particleType.getCompatibleParticleEffect().equals(event.getParticle())) continue;

                UnverifiedParticle unverifiedParticleOfType = new UnverifiedParticle(particleType, event.getParticle());

                // Check if the particle passes the verifier
                if (unverifiedParticleOfType.addNewParticle(particlePosition, event.getParticle())) {
                    unverifiedParticles.add(unverifiedParticleOfType);
                }
            }
        } else {
            List<UnverifiedParticle> invalidParticles = new ArrayList<>();

            for (UnverifiedParticle unverifiedParticle : unverifiedParticles) {
                // Check if the this type is compatible with the particle effect type
                if (!unverifiedParticle.getParticleEffect().equals(event.getParticle())) continue;

                // Check if the particle passes the verifier
                if (unverifiedParticle.addNewParticle(particlePosition, event.getParticle())) {
                    ParticleVerifier.VerificationResult verificationResult = unverifiedParticle.verifyCompleteness();

                    switch (verificationResult) {
                        case VERIFIED -> {
                            Particle particle = unverifiedParticle.getParticle();
                            WynntilsMod.postEvent(new ParticleVerifiedEvent(particle));

                            // We have verified this particle, so we can reset the unverified particles
                            unverifiedParticles.clear();
                            return;
                        }
                        case UNVERIFIED -> {
                            // Do nothing, we will wait for more particles
                        }
                        case INVALID -> {
                            invalidParticles.add(unverifiedParticle);
                        }
                    }
                } else {
                    // This must mean that either this particle is invalid or this particle type is not what we are
                    // looking for
                    invalidParticles.add(unverifiedParticle);
                }
            }

            invalidParticles.forEach(unverifiedParticles::remove);
        }
    }
}

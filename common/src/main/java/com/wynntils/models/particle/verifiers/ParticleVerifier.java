/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.particle.verifiers;

import com.wynntils.models.particle.type.Particle;
import java.util.List;
import net.minecraft.core.Position;

public interface ParticleVerifier {
    boolean verifyNewPosition(List<Position> positions, Position addedPosition);

    VerificationResult verifyCompleteness(List<Position> positions);

    Particle getParticle(List<Position> positions);

    enum VerificationResult {
        VERIFIED,
        UNVERIFIED,
        INVALID
    }
}

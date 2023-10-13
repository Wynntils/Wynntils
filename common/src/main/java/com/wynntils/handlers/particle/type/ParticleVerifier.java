/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.particle.type;

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

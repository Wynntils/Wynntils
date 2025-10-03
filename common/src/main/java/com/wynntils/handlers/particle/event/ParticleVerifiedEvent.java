/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.particle.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.handlers.particle.type.Particle;

public final class ParticleVerifiedEvent extends BaseEvent {
    private final Particle particle;

    public ParticleVerifiedEvent(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }
}

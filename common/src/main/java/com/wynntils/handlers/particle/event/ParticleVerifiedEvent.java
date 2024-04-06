/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.particle.event;

import com.wynntils.handlers.particle.type.Particle;
import net.minecraftforge.eventbus.api.Event;

public class ParticleVerifiedEvent extends Event {
    private final Particle particle;

    public ParticleVerifiedEvent(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }
}

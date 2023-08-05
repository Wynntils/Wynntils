/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.particle.event;

import com.wynntils.models.particle.type.Particle;
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

/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.neoforged.bus.api.Event;

public class ParticleAddedEvent extends Event {
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float xMaxSpeed;
    private final float yMaxSpeed;
    private final float zMaxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final ParticleOptions particle;

    public ParticleAddedEvent(ClientboundLevelParticlesPacket packet) {
        this.x = packet.x();
        this.y = packet.y();
        this.z = packet.z();
        this.xDist = packet.xDist();
        this.yDist = packet.yDist();
        this.zDist = packet.zDist();
        this.xMaxSpeed = packet.xMaxSpeed();
        this.yMaxSpeed = packet.yMaxSpeed();
        this.zMaxSpeed = packet.zMaxSpeed();
        this.count = packet.count();
        this.overrideLimiter = packet.overrideLimiter();
        this.particle = packet.particle();
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getCount() {
        return count;
    }

    public boolean isLimitOverriden() {
        return overrideLimiter;
    }

    public ParticleOptions getParticle() {
        return particle;
    }
}

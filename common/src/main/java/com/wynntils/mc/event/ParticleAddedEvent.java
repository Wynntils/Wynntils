/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

public final class ParticleAddedEvent extends BaseEvent {
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final ParticleOptions particle;

    public ParticleAddedEvent(ClientboundLevelParticlesPacket packet) {
        this.x = packet.getX();
        this.y = packet.getY();
        this.z = packet.getZ();
        this.xDist = packet.getXDist();
        this.yDist = packet.getYDist();
        this.zDist = packet.getZDist();
        this.maxSpeed = packet.getMaxSpeed();
        this.count = packet.getCount();
        this.overrideLimiter = packet.isOverrideLimiter();
        this.particle = packet.getParticle();
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

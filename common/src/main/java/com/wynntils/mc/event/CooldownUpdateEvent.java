/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class CooldownUpdateEvent extends Event {
    private final Identifier cooldownGroup;
    private final int duration;

    public CooldownUpdateEvent(Identifier cooldownGroup, int duration) {
        this.cooldownGroup = cooldownGroup;
        this.duration = duration;
    }

    public Identifier getCooldownGroup() {
        return cooldownGroup;
    }

    public int getDuration() {
        return duration;
    }
}

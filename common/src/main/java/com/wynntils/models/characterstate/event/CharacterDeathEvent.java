/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstate.event;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class CharacterDeathEvent extends Event {

    private final Vec3 deathLocation;

    public CharacterDeathEvent(Vec3 deathLocation) {
        this.deathLocation = deathLocation;
    }

    public Vec3 getDeathLocation() {
        return deathLocation;
    }

    public int getDeathX() {
        return (int) deathLocation.x;
    }

    public int getDeathY() {
        return (int) deathLocation.y;
    }

    public int getDeathZ() {
        return (int) deathLocation.z;
    }
}

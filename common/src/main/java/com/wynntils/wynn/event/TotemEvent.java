/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.mc.objects.Location;
import com.wynntils.wynn.objects.ShamanTotem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.Event;

public abstract class TotemEvent extends Event {
    private final int totemNumber;

    protected TotemEvent(int totemNumber) {
        this.totemNumber = totemNumber;
    }

    public int getTotemNumber() {
        return totemNumber;
    }

    public static class Activated extends TotemEvent {
        private final int time;
        private final Location location;

        public Activated(int totemNumber, int time, Location location) {
            super(totemNumber);
            this.time = time;
            this.location = location;
        }

        public int getTime() {
            return time;
        }

        public Location getLocation() {
            return location;
        }
    }

    public static class Removed extends TotemEvent {
        private final ShamanTotem totem;

        public Removed(int totemNumber, ShamanTotem totem) {
            super(totemNumber);
            this.totem = totem;
        }

        public ShamanTotem getTotem() {
            return totem;
        }
    }

    public static class Summoned extends TotemEvent {
        private final ArmorStand totemEntity;

        public Summoned(int totemNumber, ArmorStand totemEntity) {
            super(totemNumber);
            this.totemEntity = totemEntity;
        }

        public ArmorStand getTotemEntity() {
            return totemEntity;
        }
    }
}

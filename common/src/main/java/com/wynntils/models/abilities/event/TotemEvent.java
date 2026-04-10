/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.event;

import com.wynntils.models.abilities.type.ShamanTotem;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Display;
import net.neoforged.bus.api.Event;

public abstract class TotemEvent extends Event {
    private final int totemNumber;

    protected TotemEvent(int totemNumber) {
        this.totemNumber = totemNumber;
    }

    public int getTotemNumber() {
        return totemNumber;
    }

    /**
     * Fired when the totem's timer text display is bound to the visible totem
     */
    public static class Activated extends TotemEvent {
        private final Position position;

        public Activated(int totemNumber, Position position) {
            super(totemNumber);
            this.position = position;
        }

        public Position getPosition() {
            return position;
        }
    }

    /**
     * Fired when the totem's timer is updated (when it decreases by 1 sec)
     */
    public static class Updated extends TotemEvent {
        private final int time;
        private final Position position;

        public Updated(int totemNumber, int time, Position position) {
            super(totemNumber);
            this.time = time;
            this.position = position;
        }

        public int getTime() {
            return time;
        }

        public Position getPosition() {
            return position;
        }
    }

    /**
     * Fired when a totem is removed in-game
     */
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

    /**
     * Fired when totem is initially summoned by spell cast
     */
    public static class Summoned extends TotemEvent {
        private final Display.ItemDisplay totemEntity;

        public Summoned(int totemNumber, Display.ItemDisplay totemEntity) {
            super(totemNumber);
            this.totemEntity = totemEntity;
        }

        public Display.ItemDisplay getTotemEntity() {
            return totemEntity;
        }
    }
}

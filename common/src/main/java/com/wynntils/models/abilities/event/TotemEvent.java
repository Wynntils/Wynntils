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
     * Fired when a pending totem gets its timer text display bound.
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
     * Fired when the totem's timer is updated (when it decreases by 1 sec).
     */
    public static class Updated extends TotemEvent {
        private final int time;
        private final Position position;
        private final int transfusedAmount;
        private final String poisonAmount;

        public Updated(int totemNumber, int time, Position position, int transfusedAmount, String poisonAmount) {
            super(totemNumber);
            this.time = time;
            this.position = position;
            this.transfusedAmount = transfusedAmount;
            this.poisonAmount = poisonAmount;
        }

        public int getTime() {
            return time;
        }

        public Position getPosition() {
            return position;
        }

        public int getTransfusedAmount() {
            return transfusedAmount;
        }

        public String getPoisonAmount() {
            return poisonAmount;
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
     * Fired when the timer label first identifies a totem.
     */
    public static class Summoned extends TotemEvent {
        private final Display.TextDisplay totemTimerDisplay;

        public Summoned(int totemNumber, Display.TextDisplay totemTimerDisplay) {
            super(totemNumber);
            this.totemTimerDisplay = totemTimerDisplay;
        }

        public Display.TextDisplay getTotemTimerDisplay() {
            return totemTimerDisplay;
        }
    }
}

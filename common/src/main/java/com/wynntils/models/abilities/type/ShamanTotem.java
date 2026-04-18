/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import net.minecraft.core.Position;
import net.minecraft.world.entity.Display;

public class ShamanTotem {
    private final int totemNumber;
    private int timerEntityId;
    private int time;
    private TotemState state;
    private Display.ItemDisplay itemDisplay;
    /**
     * Our internal representation of the timer label's position.
     * */
    private Position position;

    public ShamanTotem(int totemNumber, int timerEntityId, int time, TotemState totemState, Position position) {
        this.totemNumber = totemNumber;
        this.timerEntityId = timerEntityId;
        this.time = time;
        this.state = totemState;
        this.position = position;
    }

    public int getTotemNumber() {
        return totemNumber;
    }

    public int getTimerEntityId() {
        return timerEntityId;
    }

    public void setTimerEntityId(int timerEntityId) {
        this.timerEntityId = timerEntityId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public TotemState getState() {
        return state;
    }

    public void setState(TotemState state) {
        this.state = state;
    }

    public Position getPosition() {
        return position;
    }

    public Display.ItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    public void setItemDisplay(Display.ItemDisplay itemDisplay) {
        this.itemDisplay = itemDisplay;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public enum TotemState {
        SUMMONED,
        ACTIVE
    }
}

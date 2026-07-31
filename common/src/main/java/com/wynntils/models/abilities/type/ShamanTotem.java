/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import net.minecraft.core.Position;

public class ShamanTotem {
    private final int totemNumber;
    private int timerEntityId;
    private int time;
    private TotemState state;
    private int transfusedAmount;
    private String poisonAmount;
    /**
     * Our internal representation of the timer label's position.
     * */
    private Position position;

    public ShamanTotem(
            int totemNumber,
            int timerEntityId,
            int time,
            TotemState totemState,
            Position position,
            int transfusedAmount,
            String poisonAmount) {
        this.totemNumber = totemNumber;
        this.timerEntityId = timerEntityId;
        this.time = time;
        this.state = totemState;
        this.position = position;
        this.transfusedAmount = transfusedAmount;
        this.poisonAmount = poisonAmount;
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

    public void setPosition(Position position) {
        this.position = position;
    }

    public enum TotemState {
        SUMMONED,
        ACTIVE
    }

    public int getTransfusedAmount() {
        return transfusedAmount;
    }

    public void setTransfusedAmount(int transfusedAmount) {
        this.transfusedAmount = transfusedAmount;
    }

    public String getPoisonAmount() {
        return poisonAmount;
    }

    public void setPoisonAmount(String poisonAmount) {
        this.poisonAmount = poisonAmount;
    }
}

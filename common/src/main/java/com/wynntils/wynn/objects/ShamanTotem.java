/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.wynntils.mc.objects.Location;

public class ShamanTotem {
    private int timerId;
    private int time;
    private TotemState state;
    private Location location;

    public ShamanTotem(int timerId, int time, TotemState totemState, Location location) {
        this.timerId = timerId;
        this.time = time;
        this.state = totemState;
        this.location = location;
    }

    public int getTimerId() {
        return timerId;
    }

    public void setTimerId(int timerId) {
        this.timerId = timerId;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public enum TotemState {
        SUMMONED,
        ACTIVE
    }
}

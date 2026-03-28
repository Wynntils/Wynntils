/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class MountItem extends GameItem {
    private final String name;
    private final CappedValue energy;
    private final CappedValue acceleration;
    private final CappedValue altitude;
    private final CappedValue energyStat;
    private final CappedValue handling;
    private final CappedValue powerup;
    private final CappedValue speed;
    private final CappedValue toughness;
    private final CappedValue training;

    public MountItem(
            String name,
            CappedValue energy,
            CappedValue acceleration,
            CappedValue altitude,
            CappedValue energyStat,
            CappedValue handling,
            CappedValue powerup,
            CappedValue speed,
            CappedValue toughness,
            CappedValue training) {
        this.name = name;
        this.energy = energy;
        this.acceleration = acceleration;
        this.altitude = altitude;
        this.energyStat = energyStat;
        this.handling = handling;
        this.powerup = powerup;
        this.speed = speed;
        this.toughness = toughness;
        this.training = training;
    }

    public Optional<String> getName() {
        // Name is only set if the horse is named
        return Optional.ofNullable(name);
    }

    public CappedValue getEnergy() {
        return energy;
    }

    public CappedValue getAcceleration() {
        return acceleration;
    }

    public CappedValue getAltitude() {
        return altitude;
    }

    public CappedValue getEnergyStat() {
        return energyStat;
    }

    public CappedValue getHandling() {
        return handling;
    }

    public CappedValue getPowerup() {
        return powerup;
    }

    public CappedValue getSpeed() {
        return speed;
    }

    public CappedValue getToughness() {
        return toughness;
    }

    public CappedValue getTraining() {
        return training;
    }

    @Override
    public String toString() {
        return "MountItem{" + "name='" + name + '\'' + ", energy=" + energy + ", acceleration=" + acceleration
                + ", altitude=" + altitude + ", energyStat=" + energyStat + ", handling=" + handling
                + ", powerup=" + powerup + ", speed=" + speed + ", toughness=" + toughness + ", training="
                + training + '}';
    }
}

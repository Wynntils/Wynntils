/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.type;

import com.wynntils.utils.type.RangedValue;

/**
 * Represents a state of a war tower.
 *
 * @param health      the current health of the tower
 * @param defense     the defense of the tower
 * @param damage      the damage range the tower can deal
 * @param attackSpeed the attack speed of the tower
 * @param timestamp   the timestamp of the state
 */
public record WarTowerState(long health, double defense, RangedValue damage, double attackSpeed, long timestamp) {
    public long effectiveHealth() {
        return (long) Math.floor(health / (1d - defense / 100d));
    }
}

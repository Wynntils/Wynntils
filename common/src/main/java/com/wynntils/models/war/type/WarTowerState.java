/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.type;

import com.wynntils.utils.type.RangedValue;

/**
 * Represents a state of a war tower.
 *
 * @param ownerGuild  the guild that owns the territory for this tower
 * @param territory   the territory this tower is in
 * @param health      the current health of the tower
 * @param defense     the defense of the tower
 * @param damage      the damage range the tower can deal
 * @param attackSpeed the attack speed of the tower
 */
public record WarTowerState(
        String ownerGuild, String territory, long health, int defense, RangedValue damage, double attackSpeed) {}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

public enum CombatClickType {
    PRIMARY,
    SECONDARY,
    MELEE;

    public boolean usesRightClick(boolean isArcher) {
        return switch (this) {
            case PRIMARY -> !isArcher;
            case SECONDARY, MELEE -> isArcher;
        };
    }
}

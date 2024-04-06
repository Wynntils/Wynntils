/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

public record ItemInformation(int itemId, int damage) {
    public int getBlockedDamage() {
        return damage + 3;
    }

    public int getLockedDamage() {
        return damage;
    }

    public int getUnlockableDamage() {
        return damage + 1;
    }

    public int getUnlockedDamage() {
        return damage + 2;
    }
}

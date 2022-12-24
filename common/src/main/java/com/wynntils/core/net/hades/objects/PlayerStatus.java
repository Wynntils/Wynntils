/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.hades.objects;

public record PlayerStatus(float x, float y, float z, int health, int maxHealth, int mana, int maxMana) {
    public boolean equals(float x, float y, float z, int health, int mana, int maxHealth, int maxMana) {
        return this.x == x
                && this.y == y
                && this.z == z
                && this.health == health
                && this.maxHealth == maxHealth
                && this.mana == mana
                && this.maxMana == maxMana;
    }
}

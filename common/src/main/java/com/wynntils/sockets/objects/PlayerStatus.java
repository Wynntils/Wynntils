/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.objects;

public record PlayerStatus(double x, double y, double z, int health, int mana, int maxHealth, int maxMana) {
    public boolean equals(double x, double y, double z, int health, int mana, int maxHealth, int maxMana) {
        return this.x == x
                && this.y == y
                && this.z == z
                && this.health == health
                && this.maxHealth == maxHealth
                && this.mana == mana
                && this.maxMana == maxMana;
    }
}

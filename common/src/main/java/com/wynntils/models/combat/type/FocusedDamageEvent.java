/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.type;

import net.neoforged.bus.api.Event;

/**
 * These events are sent when Wynncraft sets up a boss bar for the mob which
 * is the target of the player's attacks. MobFocused is sent when a new mob
 * is tracked by the boss bar, and MobDamaged when the same mob just loses health.
 *
 * Note that other players or environmental damage can also cause the mob to lose
 * health, so the difference in health for MobDamaged might not be due to the current
 * player.
 */
public abstract class FocusedDamageEvent extends Event {
    private final String mobName;
    private final MobElementals mobElementals;
    private final long health;

    protected FocusedDamageEvent(String mobName, MobElementals mobElementals, long health) {
        this.mobName = mobName;
        this.mobElementals = mobElementals;
        this.health = health;
    }

    public String getMobName() {
        return mobName;
    }

    public MobElementals getMobElementals() {
        return mobElementals;
    }

    public long getHealth() {
        return health;
    }

    public static final class MobFocused extends FocusedDamageEvent {
        public MobFocused(String mobName, MobElementals mobElementals, long health) {
            super(mobName, mobElementals, health);
        }
    }

    public static final class MobDamaged extends FocusedDamageEvent {
        private final long oldHealth;

        public MobDamaged(String mobName, MobElementals mobElementals, long currentHealth, long oldHealth) {
            super(mobName, mobElementals, currentHealth);
            this.oldHealth = oldHealth;
        }

        public long getOldHealth() {
            return oldHealth;
        }
    }
}

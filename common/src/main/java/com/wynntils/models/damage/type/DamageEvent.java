/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage.type;

import net.minecraftforge.eventbus.api.Event;

public abstract class DamageEvent extends Event {
    private final String mobName;
    private final String mobElementals;
    private final int health;

    protected DamageEvent(String mobName, String mobElementals, int health) {
        this.mobName = mobName;
        this.mobElementals = mobElementals;
        this.health = health;
    }

    public String getMobName() {
        return mobName;
    }

    public String getMobElementals() {
        return mobElementals;
    }

    public int getHealth() {
        return health;
    }

    public static final class MobFocused extends DamageEvent {
        public MobFocused(String mobName, String mobElementals, int health) {
            super(mobName, mobElementals, health);
        }
    }

    public static final class MobDamaged extends DamageEvent {
        private final int oldHealth;

        public MobDamaged(String mobName, String mobElementals, int currentHealth, int oldHealth) {
            super(mobName, mobElementals, currentHealth);
            this.oldHealth = oldHealth;
        }

        public int getOldHealth() {
            return oldHealth;
        }
    }
}

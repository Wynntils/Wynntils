/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import java.util.Locale;

public enum GearMiscStat implements GearStat {
    HEALTH("Health", null, "healthBonus"),
    HEALTH_REGEN_PERCENT("Health Regen", "%", "healthRegen"),
    HEALTH_REGEN_RAW("Health Regen", null, "healthRegenRaw"),
    LIFE_STEAL("Life Steal", "/3s", "lifeSteal"),

    MANA_REGEN("Mana Regen", "/5s", "manaRegen"),
    MANA_STEAL("Mana Steal", "/3s", "manaSteal"),

    WALK_SPEED("Walk Speed", "%", "speed"),
    SPRINT("Sprint", "%", "sprint", "STAMINA"),
    SPRINT_REGEN("Sprint Regen", "%", "sprintRegen", "STAMINA_REGEN"),

    THORNS("Thorns", "%", "thorns"),
    EXPLODING("Exploding", "%", "exploding"),
    POISON("Poison", "/3s", "poison"),
    REFLECTION("Reflection", "%", "reflection"),

    STEALING("Stealing", "%", "emeraldStealing"),
    ATTACK_SPEED("Attack Speed", " tier", "attackSpeedBonus", "ATTACKSPEED"),
    JUMP_HEIGHT("Jump Height", null, "jumpHeight", "JUMP_HEIGHT"),
    LOOT_BONUS("Loot Bonus", "%", "lootBonus"),
    SOUL_POINT_REGEN("Soul Point Regen", "%", "soulPoints"),
    XP_BONUS("XP Bonus", "%", "xpBonus");

    private final String displayName;
    private final String unit;
    private final String loreName;
    private final String apiName;

    GearMiscStat(String displayName, String unit, String apiName, String loreName) {
        this.displayName = displayName;
        this.unit = unit;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    GearMiscStat(String displayName, String unit, String apiName) {
        this(displayName, unit, apiName, apiName.toUpperCase(Locale.ROOT));
    }

    @Override
    public String getKey() {
        return this.name();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}

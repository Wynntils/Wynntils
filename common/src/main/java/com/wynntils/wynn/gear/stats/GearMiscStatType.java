/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import java.util.Locale;

public enum GearMiscStatType {
    HEALTH("Health", "healthBonus", GearStatUnit.RAW),
    HEALTH_REGEN_PERCENT("Health Regen", "healthRegen", GearStatUnit.PERCENT),
    HEALTH_REGEN_RAW("Health Regen", "healthRegenRaw", GearStatUnit.RAW),
    LIFE_STEAL("Life Steal", "lifeSteal", GearStatUnit.PER_3_S),

    MANA_REGEN("Mana Regen", "manaRegen", GearStatUnit.PER_5_S),
    MANA_STEAL("Mana Steal", "manaSteal", GearStatUnit.PER_3_S),

    WALK_SPEED("Walk Speed", "speed", GearStatUnit.PERCENT),
    SPRINT("Sprint", "sprint", GearStatUnit.PERCENT, "STAMINA"),
    SPRINT_REGEN("Sprint Regen", "sprintRegen", GearStatUnit.PERCENT, "STAMINA_REGEN"),

    THORNS("Thorns", "thorns", GearStatUnit.PERCENT),
    EXPLODING("Exploding", "exploding", GearStatUnit.PERCENT),
    POISON("Poison", "poison", GearStatUnit.PER_3_S),
    REFLECTION("Reflection", "reflection", GearStatUnit.PERCENT),

    STEALING("Stealing", "emeraldStealing", GearStatUnit.PERCENT),
    ATTACK_SPEED("Attack Speed", "attackSpeedBonus", GearStatUnit.TIER, "ATTACKSPEED"),
    JUMP_HEIGHT("Jump Height", "jumpHeight", GearStatUnit.RAW, "JUMP_HEIGHT"),
    LOOT_BONUS("Loot Bonus", "lootBonus", GearStatUnit.PERCENT),
    SOUL_POINT_REGEN("Soul Point Regen", "soulPoints", GearStatUnit.PERCENT),
    XP_BONUS("XP Bonus", "xpBonus", GearStatUnit.PERCENT);

    private final String displayName;
    private final String apiName;
    private final GearStatUnit unit;
    private final String loreName;

    GearMiscStatType(String displayName, String apiName, GearStatUnit unit, String loreName) {
        this.displayName = displayName;
        this.apiName = apiName;
        this.unit = unit;
        this.loreName = loreName;
    }

    GearMiscStatType(String displayName, String apiName, GearStatUnit unit) {
        this(displayName, apiName, unit, apiName.toUpperCase(Locale.ROOT));
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getLoreName() {
        return loreName;
    }

    public GearStatUnit getUnit() {
        return unit;
    }
}

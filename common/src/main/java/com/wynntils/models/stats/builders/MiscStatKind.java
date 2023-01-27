/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.stats.type.StatUnit;
import java.util.Locale;

public enum MiscStatKind {
    HEALTH("Health", StatUnit.RAW, "healthBonus"),
    HEALTH_REGEN_PERCENT("Health Regen", StatUnit.PERCENT, "healthRegen"),
    HEALTH_REGEN_RAW("Health Regen", StatUnit.RAW, "healthRegenRaw"),
    LIFE_STEAL("Life Steal", StatUnit.PER_3_S, "lifeSteal"),

    MANA_REGEN("Mana Regen", StatUnit.PER_5_S, "manaRegen"),
    MANA_STEAL("Mana Steal", StatUnit.PER_3_S, "manaSteal"),

    WALK_SPEED("Walk Speed", StatUnit.PERCENT, "speed"),
    SPRINT("Sprint", StatUnit.PERCENT, "sprint", "STAMINA"),
    SPRINT_REGEN("Sprint Regen", StatUnit.PERCENT, "sprintRegen", "STAMINA_REGEN"),

    THORNS("Thorns", StatUnit.PERCENT, "thorns"),
    EXPLODING("Exploding", StatUnit.PERCENT, "exploding"),
    POISON("Poison", StatUnit.PER_3_S, "poison"),
    REFLECTION("Reflection", StatUnit.PERCENT, "reflection"),

    STEALING("Stealing", StatUnit.PERCENT, "emeraldStealing"),
    ATTACK_SPEED("Attack Speed", StatUnit.TIER, "attackSpeedBonus", "ATTACKSPEED"),
    JUMP_HEIGHT("Jump Height", StatUnit.RAW, "jumpHeight", "JUMP_HEIGHT"),
    LOOT_BONUS("Loot Bonus", StatUnit.PERCENT, "lootBonus"),
    SOUL_POINT_REGEN("Soul Point Regen", StatUnit.PERCENT, "soulPoints"),
    XP_BONUS("XP Bonus", StatUnit.PERCENT, "xpBonus"),

    // These are only found on crafted gear
    LOOT_QUALITY("Loot Quality", StatUnit.PERCENT, "lootQuality"),
    GATHER_XP_BONUS("Gather XP Bonus", StatUnit.PERCENT, "gatherXpBonus"),
    GATHER_SPEED("Gather Speed", StatUnit.PERCENT, "gatherSpeed");

    private final String displayName;
    private final String apiName;
    private final StatUnit unit;
    private final String loreName;

    MiscStatKind(String displayName, StatUnit unit, String apiName, String loreName) {
        this.displayName = displayName;
        this.apiName = apiName;
        this.unit = unit;
        this.loreName = loreName;
    }

    MiscStatKind(String displayName, StatUnit unit, String apiName) {
        this(displayName, unit, apiName, apiName.toUpperCase(Locale.ROOT));
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

    public StatUnit getUnit() {
        return unit;
    }
}

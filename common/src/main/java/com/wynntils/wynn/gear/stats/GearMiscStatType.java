/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.types.GearStatUnit;
import java.util.Locale;

public enum GearMiscStatType {
    HEALTH("Health", GearStatUnit.RAW, "healthBonus"),
    HEALTH_REGEN_PERCENT("Health Regen", GearStatUnit.PERCENT, "healthRegen"),
    HEALTH_REGEN_RAW("Health Regen", GearStatUnit.RAW, "healthRegenRaw"),
    LIFE_STEAL("Life Steal", GearStatUnit.PER_3_S, "lifeSteal"),

    MANA_REGEN("Mana Regen", GearStatUnit.PER_5_S, "manaRegen"),
    MANA_STEAL("Mana Steal", GearStatUnit.PER_3_S, "manaSteal"),

    WALK_SPEED("Walk Speed", GearStatUnit.PERCENT, "speed"),
    SPRINT("Sprint", GearStatUnit.PERCENT, "sprint", "STAMINA"),
    SPRINT_REGEN("Sprint Regen", GearStatUnit.PERCENT, "sprintRegen", "STAMINA_REGEN"),

    THORNS("Thorns", GearStatUnit.PERCENT, "thorns"),
    EXPLODING("Exploding", GearStatUnit.PERCENT, "exploding"),
    POISON("Poison", GearStatUnit.PER_3_S, "poison"),
    REFLECTION("Reflection", GearStatUnit.PERCENT, "reflection"),

    STEALING("Stealing", GearStatUnit.PERCENT, "emeraldStealing"),
    ATTACK_SPEED("Attack Speed", GearStatUnit.TIER, "attackSpeedBonus", "ATTACKSPEED"),
    JUMP_HEIGHT("Jump Height", GearStatUnit.RAW, "jumpHeight", "JUMP_HEIGHT"),
    LOOT_BONUS("Loot Bonus", GearStatUnit.PERCENT, "lootBonus"),
    SOUL_POINT_REGEN("Soul Point Regen", GearStatUnit.PERCENT, "soulPoints"),
    XP_BONUS("XP Bonus", GearStatUnit.PERCENT, "xpBonus"),

    // These are only found on crafted gear
    LOOT_QUALITY("Loot Quality", GearStatUnit.PERCENT, "<unknown1>"),
    GATHER_XP_BONUS("Gather XP Bonus", GearStatUnit.PERCENT, "<unknown2>"),
    GATHER_SPEED("Gather Speed", GearStatUnit.PERCENT, "<unknown3>");


    private final String displayName;
    private final String apiName;
    private final GearStatUnit unit;
    private final String loreName;

    GearMiscStatType(String displayName, GearStatUnit unit, String apiName, String loreName) {
        this.displayName = displayName;
        this.apiName = apiName;
        this.unit = unit;
        this.loreName = loreName;
    }

    GearMiscStatType(String displayName, GearStatUnit unit, String apiName) {
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

    public GearStatUnit getUnit() {
        return unit;
    }
}

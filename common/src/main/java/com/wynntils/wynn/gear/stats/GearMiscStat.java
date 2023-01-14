/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public enum GearMiscStat implements GearStat {
    /*
    THESE HAVE SUSPICIOUS LORE:

    ALSO, ALL SPELL COST...

    DAMAGE_ELEMANTAL
    DAMAGE_ELEMENTAL
    DAMAGE_MAIN_ATTACK_ELEMENTAL
    DAMAGE_MAIN_ATTACK_PERCENT
    DAMAGE_MAIN_ATTACK_RAW
    DAMAGE_SPELL_PERCENT
    DAMAGE_SPELL_RAW
    SPRINT
    SPRINT_REGEN
    ATTACK_SPEED
    JUMP_HEIGHT
    DEFENCE_AIR
    DEFENCE_EARTH
    DEFENCE_FIRE
    DEFENCE_THUNDER
    DEFENCE_WATER
    DAMAGE_AIR_RAW
    DAMAGE_EARTH_RAW
    DAMAGE_FIRE_RAW
    DAMAGE_THUNDER_RAW
    DAMAGE_WATER_RAW
    DAMAGE_SPELL_AIR_PERCENT
    DAMAGE_SPELL_EARTH_PERCENT
    DAMAGE_SPELL_FIRE_PERCENT
    DAMAGE_SPELL_THUNDER_PERCENT
    DAMAGE_SPELL_WATER_PERCENT
    DAMAGE_MAIN_AIR_RAW
    DAMAGE_MAIN_EARTH_RAW
    DAMAGE_MAIN_FIRE_RAW
    DAMAGE_MAIN_THUNDER_RAW
    DAMAGE_MAIN_WATER_RAW
    DAMAGE_MAIN_AIR_PERCENT
    DAMAGE_MAIN_EARTH_PERCENT
    DAMAGE_MAIN_FIRE_PERCENT
    DAMAGE_MAIN_THUNDER_PERCENT
    DAMAGE_MAIN_WATER_PERCENT
     */

    HEALTH(IsVariable.YES, "Health", null, "rawHealth", "HEALTHBONUS", "healthBonus"),
    HEALTH_REGEN_PERCENT(IsVariable.YES, "Health Regen", "%", "healthRegen", "HEALTHREGEN", "healthRegen"),
    HEALTH_REGEN_RAW(IsVariable.YES, "Health Regen", null, "rawHealthRegen", "HEALTHREGENRAW", "healthRegenRaw"),
    LIFE_STEAL(IsVariable.YES, "Life Steal", "/3s", "lifeSteal", "LIFESTEAL", "lifeSteal"),

    MANA_REGEN(IsVariable.YES, "Mana Regen", "/5s", "manaRegen", "MANAREGEN", "manaRegen"),
    MANA_STEAL(IsVariable.YES, "Mana Steal", "/3s", "manaSteal", "MANASTEAL", "manaSteal"),

    WALK_SPEED(IsVariable.YES, "Walk Speed", "%", "walkSpeed", "SPEED", "speed"),
    // FIXME: is lore correct for these two?
    SPRINT(IsVariable.YES, "Sprint", "%", "sprint", "STAMINA", "sprint"),
    SPRINT_REGEN(IsVariable.YES, "Sprint Regen", "%", "sprintRegen", "STAMINA_REGEN", "sprintRegen"),

    THORNS(IsVariable.YES, "Thorns", "%", "thorns", "THORNS", "thorns"),
    EXPLODING(IsVariable.YES, "Exploding", "%", "exploding", "EXPLODING", "exploding"),
    POISON(IsVariable.YES, "Poison", "/3s", "poison", "POISON", "poison"),
    REFLECTION(IsVariable.YES, "Reflection", "%", "reflection", "REFLECTION", "reflection"),

    STEALING(IsVariable.YES, "Stealing", "%", "stealing", "EMERALDSTEALING", "emeraldStealing"),
    // FIXME: is lore correct?
    ATTACK_SPEED(IsVariable.YES, "Attack Speed", " tier", "attackSpeed", "ATTACKSPEED", "attackSpeedBonus"),
    // FIXME: is lore correct?
    JUMP_HEIGHT(IsVariable.YES, "Jump Height", null, "rawJumpHeight", "JUMP_HEIGHT", "jumpHeight"),
    LOOT_BONUS(IsVariable.YES, "Loot Bonus", "%", "lootBonus", "LOOTBONUS", "lootBonus"),
    SOUL_POINT_REGEN(IsVariable.YES, "Soul Point Regen", "%", "soulPointRegen", "SOULPOINTS", "soulPoints"),
    XP_BONUS(IsVariable.YES, "XP Bonus", "%", "xpBonus", "XPBONUS", "xpBonus");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    GearMiscStat(
            IsVariable isVariable,
            String displayName,
            String unit,
            String athenaName,
            String loreName,
            String apiName) {
        this.isVariable = isVariable;
        this.displayName = displayName;
        this.unit = unit;
        this.athenaName = athenaName;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    @Override
    public String getKey() {
        return this.name();
    }

    public IsVariable getIsVariable() {
        return isVariable;
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
    public String getAthenaName() {
        return athenaName;
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

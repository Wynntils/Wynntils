/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public enum IdMiscTypes implements IdType {
    // FIXME: missing from Athena. Check lore name, e.g. Aleph null.
    DAMAGE_ELEMANTAL(IsVariable.YES, "Elemental Damage", "%", "FIXME:MISSING", "FIXME:UNKNWON", "elementalDamageBonus"),

    // FIXME
    DAMAGE_SPELL_ELEMENTAL_PERCENT(
            IsVariable.YES,
            "Elemental Spell Damage",
            "%",
            "elementalSpellDamage",
            "SPELLELEMENTALDAMAGEBONUS",
            "spellElementalDamageBonus"),
    // FIXME, check with only available item: Forest Aconite, untradable
    DAMAGE_SPELL_ELEMENTAL_RAW(
            IsVariable.YES,
            "Elemental Spell Damage",
            null,
            "rawElementalSpellDamage",
            "SPELLELEMENTALDAMAGEBONUSRAW",
            "spellElementalDamageBonusRaw"),

    // FIXME
    DAMAGE_SPELL_NEUTRAL(
            IsVariable.YES,
            "Neutral Spell Damage",
            null,
            "rawNeutralSpellDamage",
            "SPELLNEUTRALDAMAGEBONUSRAW",
            "FIXME"),
    DAMAGE_MAIN_ATTACK_PERCENT(
            IsVariable.YES, "Main Attack Damage", "%", "mainAttackDamage", "DAMAGEBONUS", "damageBonus"),
    DAMAGE_MAIN_ATTACK_RAW(
            IsVariable.YES, "Main Attack Damage", null, "rawMainAttackDamage", "DAMAGEBONUSRAW", "damageBonusRaw"),
    DAMAGE_SPELL_PERCENT(IsVariable.YES, "Spell Damage", "%", "spellDamage", "SPELLDAMAGE", "spellDamage"),
    DAMAGE_SPELL_RAW(IsVariable.YES, "Spell Damage", null, "rawSpellDamage", "SPELLDAMAGERAW", "spellDamageRaw"),

    HEALTH(IsVariable.YES, "Health", null, "rawHealth", "HEALTHBONUS", "healthBonus"),
    HEALTH_REGEN_PERCENT(IsVariable.YES, "Health Regen", "%", "healthRegen", "HEALTHREGEN", "healthRegen"),
    HEALTH_REGEN_RAW(IsVariable.YES, "Health Regen", null, "rawHealthRegen", "HEALTHREGENRAW", "healthRegenRaw"),
    LIFE_STEAL(IsVariable.YES, "Life Steal", "/3s", "lifeSteal", "LIFESTEAL", "lifeSteal"),

    MANA_REGEN(IsVariable.YES, "Mana Regen", "/5s", "manaRegen", "MANAREGEN", "manaRegen"),
    MANA_STEAL(IsVariable.YES, "Mana Steal", "/3s", "manaSteal", "MANASTEAL", "manaSteal"),

    WALK_SPEED(IsVariable.YES, "Walk Speed", "%", "walkSpeed", "SPEED", "speed"),
    // FIXME
    SPRINT(IsVariable.YES, "Sprint", "%", "sprint", "STAMINA", "FIXME"),
    // FIXME
    SPRINT_REGEN(IsVariable.YES, "Sprint Regen", "%", "sprintRegen", "STAMINA_REGEN", "FIXME"),

    THORNS(IsVariable.YES, "Thorns", "%", "thorns", "THORNS", "thorns"),
    EXPLODING(IsVariable.YES, "Exploding", "%", "exploding", "EXPLODING", "exploding"),
    POISON(IsVariable.YES, "Poison", "/3s", "poison", "POISON", "poison"),
    REFLECTION(IsVariable.YES, "Reflection", "%", "reflection", "REFLECTION", "reflection"),

    STEALING(IsVariable.YES, "Stealing", "%", "stealing", "EMERALDSTEALING", "emeraldStealing"),
    // FIXME: Check discrepancy between LORE and api
    ATTACK_SPEED(IsVariable.YES, "Attack Speed", " tier", "attackSpeed", "ATTACKSPEED", "attackSpeedBonus"),
    // FIXME
    JUMP_HEIGHT(IsVariable.YES, "Jump Height", null, "rawJumpHeight", "JUMP_HEIGHT", "FIXME"),
    LOOT_BONUS(IsVariable.YES, "Loot Bonus", "%", "lootBonus", "LOOTBONUS", "lootBonus"),
    SOUL_POINT_REGEN(IsVariable.YES, "Soul Point Regen", "%", "soulPointRegen", "SOULPOINTS", "soulPoints"),
    XP_BONUS(IsVariable.YES, "XP Bonus", "%", "xpBonus", "XPBONUS", "xpBonus");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    IdMiscTypes(
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

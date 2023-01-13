/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public enum IdStatTypes implements IdType {
    SKILL_AGILITY(IsVariable.NO, "Agility", null, "rawAgility", "AGILITYPOINTS", "agilityPoints"),
    SKILL_DEFENCE(IsVariable.NO, "Defence", null, "rawDefence", "DEFENSEPOINTS", "defensePoints"),
    SKILL_DEXTERITY(IsVariable.NO, "Dexterity", null, "rawDexterity", "DEXTERITYPOINTS", "dexterityPoints"),
    SKILL_INTELLIGENCE(
            IsVariable.NO, "Intelligence", null, "rawIntelligence", "INTELLIGENCEPOINTS", "intelligencePoints"),
    SKILL_STRENGTH(IsVariable.NO, "Strength", null, "rawStrength", "STRENGTHPOINTS", "strengthPoints"),

    // FIXME: check discrepancy!
    DEFENCE_AIR(IsVariable.YES, "Air Defence", "%", "airDefence", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH(IsVariable.YES, "Earth Defence", "%", "earthDefence", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE(IsVariable.YES, "Fire Defence", "%", "fireDefence", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER(IsVariable.YES, "Thunder Defence", "%", "thunderDefence", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER(IsVariable.YES, "Water Defence", "%", "waterDefence", "WATERDEFENSE", "bonusWaterDefense"),

    // FIXME: check discrepancy!
    DAMAGE_AIR(IsVariable.YES, "Air Damage", "%", "airDamage", "AIRDAMAGEBONUS", "bonusAirDamage"),
    DAMAGE_EARTH(IsVariable.YES, "Earth Damage", "%", "earthDamage", "EARTHDAMAGEBONUS", "bonusEarthDamage"),
    DAMAGE_FIRE(IsVariable.YES, "Fire Damage", "%", "fireDamage", "FIREDAMAGEBONUS", "bonusFireDamage"),
    DAMAGE_THUNDER(IsVariable.YES, "Thunder Damage", "%", "thunderDamage", "THUNDERDAMAGEBONUS", "bonusThunderDamage"),
    DAMAGE_WATER(IsVariable.YES, "Water Damage", "%", "waterDamage", "WATERDAMAGEBONUS", "bonusWaterDamage"),

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

    // FIXME: this is e.g. on Soul Ink. Afaict, Athena is missing this. Check lore name!
    DAMAGE_SPELL_AIR_PERCENT(
            IsVariable.YES, "Air Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellAirDamageBonus"),

    // Note: these are untested, but assumed...
    DAMAGE_SPELL_EARTH_PERCENT(
            IsVariable.YES, "Earth Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT(
            IsVariable.YES, "Fire Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT(
            IsVariable.YES, "Thunder Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellThunderDamageBonus"),
    DAMAGE_SPELL_WATER_PERCENT(
            IsVariable.YES, "Water Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellWaterDamageBonus"),

    // FIXME: this is e.g. on Soul Ink.
    DAMAGE_SPELL_WATER_RAW(
            IsVariable.YES,
            "Water Spell Damage",
            null,
            "rawWaterSpellDamage",
            "SPELLWATERDAMAGEBONUSRAW",
            "spellWaterDamageBonusRaw"),
    DAMAGE_SPELL_EARTH_RAW(
            IsVariable.YES,
            "Earth Spell Damage",
            null,
            "rawEarthSpellDamage",
            "SPELLEARTHDAMAGEBONUSRAW",
            "spellEarthDamageBonusRaw"),
    DAMAGE_SPELL_FIRE_RAW(
            IsVariable.YES,
            "Fire Spell Damage",
            null,
            "rawFireSpellDamage",
            "SPELLFIREDAMAGEBONUSRAW",
            "spellFireDamageBonusRaw"),
    DAMAGE_SPELL_THUNDER_RAW(
            IsVariable.YES,
            "Thunder Spell Damage",
            null,
            "rawThunderSpellDamage",
            "SPELLTHUNDERDAMAGEBONUSRAW",
            "spellThunderDamageBonusRaw"),
    DAMAGE_SPELL_AIR_RAW(
            IsVariable.YES,
            "Air Spell Damage",
            null,
            "rawAirSpellDamage",
            "SPELLAIRDAMAGEBONUSRAW",
            "spellAirDamageBonusRaw"),

    // FIXME: should perhaps be named _RAW? Do we have percent? And is the LORE name from old Athena really correct???
    // Check lore with e.g. Spearmint
    DAMAGE_MAIN_AIR_RAW(
            IsVariable.YES, "Air Main Attack Damage", null, "FIXME", "FIXME", "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH_RAW(
            IsVariable.YES, "Earth Main Attack Damage", null, "FIXME", "FIXME", "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE_RAW(
            IsVariable.YES, "Fire Main Attack Damage", null, "FIXME", "FIXME", "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER_RAW(
            IsVariable.YES, "Thunder Main Attack Damage", null, "FIXME", "FIXME", "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER_RAW(
            IsVariable.YES, "Water Main Attack Damage", null, "FIXME", "FIXME", "mainAttackWaterDamageBonusRaw"),

    DAMAGE_MAIN_AIR_PERCENT(
            IsVariable.YES, "Air Main Attack Damage", "%", "airDamage", "FIXME:UNKNOWN", "mainAttackAirDamageBonus"),
    DAMAGE_MAIN_EARTH_PERCENT(
            IsVariable.YES,
            "Earth Main Attack Damage",
            "%",
            "earthDamage",
            "FIXME:UNKNOWN",
            "mainAttackEarthDamageBonus"),
    DAMAGE_MAIN_FIRE_PERCENT(
            IsVariable.YES, "Fire Main Attack Damage", "%", "fireDamage", "FIXME:UNKNOWN", "mainAttackFireDamageBonus"),
    DAMAGE_MAIN_THUNDER_PERCENT(
            IsVariable.YES,
            "Thunder Main Attack Damage",
            "%",
            "thunderDamage",
            "FIXME:UNKNOWN",
            "mainAttackThunderDamageBonus"),
    DAMAGE_MAIN_WATER_PERCENT(
            IsVariable.YES,
            "Water Main Attack Damage",
            "%",
            "waterDamage",
            "FIXME:UNKNOWN",
            "mainAttackWaterDamageBonus"),

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

    IdStatTypes(
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

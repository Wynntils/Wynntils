/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public enum GearElementalStat implements GearStat {
    // FIXME: check discrepancy between lore and api
    DEFENCE_AIR(IsVariable.YES, "Air Defence", "%", "airDefence", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH(IsVariable.YES, "Earth Defence", "%", "earthDefence", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE(IsVariable.YES, "Fire Defence", "%", "fireDefence", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER(IsVariable.YES, "Thunder Defence", "%", "thunderDefence", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER(IsVariable.YES, "Water Defence", "%", "waterDefence", "WATERDEFENSE", "bonusWaterDefense"),

    DAMAGE_AIR_PERCENT(IsVariable.YES, "Air Damage", "%", "airDamage", "AIRDAMAGEBONUS", "airDamageBonus"),
    DAMAGE_EARTH_PERCENT(IsVariable.YES, "Earth Damage", "%", "earthDamage", "EARTHDAMAGEBONUS", "earthDamageBonus"),
    DAMAGE_FIRE_PERCENT(IsVariable.YES, "Fire Damage", "%", "fireDamage", "FIREDAMAGEBONUS", "fireDamageBonus"),
    DAMAGE_THUNDER_PERCENT(
            IsVariable.YES, "Thunder Damage", "%", "thunderDamage", "THUNDERDAMAGEBONUS", "thunderDamageBonus"),
    DAMAGE_WATER_PERCENT(IsVariable.YES, "Water Damage", "%", "waterDamage", "WATERDAMAGEBONUS", "waterDamageBonus"),

    // FIXME: missing from Athena, check lore
    DAMAGE_AIR_RAW(IsVariable.YES, "Air Damage", "%", "airDamage", "FIXME:UNKNOWN", "airDamageBonusRaw"),
    DAMAGE_EARTH_RAW(IsVariable.YES, "Earth Damage", "%", "earthDamage", "FIXME:UNKNOWN", "earthDamageBonusRaw"),
    DAMAGE_FIRE_RAW(IsVariable.YES, "Fire Damage", "%", "fireDamage", "FIXME:UNKNOWN", "fireDamageBonusRaw"),
    DAMAGE_THUNDER_RAW(
            IsVariable.YES, "Thunder Damage", "%", "thunderDamage", "FIXME:UNKNOWN", "thunderDamageBonusRaw"),
    DAMAGE_WATER_RAW(IsVariable.YES, "Water Damage", "%", "waterDamage", "FIXME:UNKNOWN", "waterDamageBonusRaw"),

    // FIXME: this is e.g. on Soul Ink. Afaict, Athena is missing this. Check lore name!
    DAMAGE_SPELL_AIR_PERCENT(
            IsVariable.YES, "Air Spell Damage", "%", "MISSING", "FIXME:UNKNOWN", "spellAirDamageBonus"),
    // FIXME: check lore on e.g Decaying Headdress
    DAMAGE_SPELL_EARTH_PERCENT(
            IsVariable.YES, "Earth Spell Damage", "%", "MISSING", "FIXME:UNKNOWN", "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT(
            IsVariable.YES, "Fire Spell Damage", "%", "MISSING", "FIXME:UNKNOWN", "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT(
            IsVariable.YES, "Thunder Spell Damage", "%", "MISSING", "FIXME:UNKNOWN", "spellThunderDamageBonus"),
    // FIXME: check lore on eg Ancient Waters
    DAMAGE_SPELL_WATER_PERCENT(
            IsVariable.YES, "Water Spell Damage", "%", "MISSING", "FIXME:UNKNOWN", "spellWaterDamageBonus"),

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

    // Check lore with e.g. Spearmint or Darting Blur. Missing from Athena
    DAMAGE_MAIN_AIR_RAW(
            IsVariable.YES, "Air Main Attack Damage", null, "MISSING", "FIXME:UNKNOWN", "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH_RAW(
            IsVariable.YES,
            "Earth Main Attack Damage",
            null,
            "MISSING",
            "FIXME:UNKNOWN",
            "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE_RAW(
            IsVariable.YES,
            "Fire Main Attack Damage",
            null,
            "MISSING",
            "FIXME:UNKNOWN",
            "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER_RAW(
            IsVariable.YES,
            "Thunder Main Attack Damage",
            null,
            "MISSING",
            "FIXME:UNKNOWN",
            "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER_RAW(
            IsVariable.YES,
            "Water Main Attack Damage",
            null,
            "MISSING",
            "FIXME:UNKNOWN",
            "mainAttackWaterDamageBonusRaw"),

    // FIXME: check lore with Wind Spine
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
            "mainAttackWaterDamageBonus");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    GearElementalStat(
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

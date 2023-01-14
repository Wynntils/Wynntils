/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public enum IdElementalTypes implements IdType {
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

    // FIXME: And is the LORE name from old Athena really correct???
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
            "mainAttackWaterDamageBonus");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    IdElementalTypes(
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

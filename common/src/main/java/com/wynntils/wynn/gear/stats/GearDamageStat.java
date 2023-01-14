/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public enum GearDamageStat implements GearStat {

    // DISPLAY NAME:
    // <element> <type> Damage
    // <element> is:
    // missing -- goes for all
    // Neutral
    // Elemental -- rainbow
    // Air,Thunder,etc -- this element

    // Type is:
    // missing -- goes for all
    // Spell
    // Main Attack

    // api name: lowerCamelCase
    // <type> <element> DamageBonus[Raw]

    // ELEMENT UNSPECIFIED, ATTACK TYPE SPECIFIED
    // FIXME: lore???
    DAMAGE_SPELL_PERCENT("Spell Damage", "%", "SPELLDAMAGE", "spellDamageBonus"),
    DAMAGE_SPELL_RAW("Spell Damage", null, "SPELLDAMAGERAW", "spellDamageBonusRaw"),

    // FIXME: lore??
    DAMAGE_MAIN_ATTACK_PERCENT("Main Attack Damage", "%", "DAMAGEBONUS", "mainAttackDamageBonus"),
    DAMAGE_MAIN_ATTACK_RAW("Main Attack Damage", null, "DAMAGEBONUSRAW", "mainAttackDamageBonusRaw"),

    // ATTACK TYPE UNSPECIFIED, ELEMENT SPECIFIED

    // rainbow
    // FIXME: missing from Athena. Check lore name, with e.g. Aleph null.
    DAMAGE_ELEMANTAL_PERCENT("Elemental Damage", "%", "FIXME:UNKNOWN", "elementalDamageBonus"),
    // FIXME: these two: check Axion. Missing in Athena.
    DAMAGE_ELEMENTAL_RAW("FIXME:UNKNOWN", null, "FIXME:UNKNOWN", "elementalDamageBonusRaw"),

    // ELEMENTAL

    DAMAGE_AIR_PERCENT("Air Damage", "%", "AIRDAMAGEBONUS", "airDamageBonus"),
    DAMAGE_EARTH_PERCENT("Earth Damage", "%", "EARTHDAMAGEBONUS", "earthDamageBonus"),
    DAMAGE_FIRE_PERCENT("Fire Damage", "%", "FIREDAMAGEBONUS", "fireDamageBonus"),
    DAMAGE_THUNDER_PERCENT("Thunder Damage", "%", "THUNDERDAMAGEBONUS", "thunderDamageBonus"),
    DAMAGE_WATER_PERCENT("Water Damage", "%", "WATERDAMAGEBONUS", "waterDamageBonus"),

    // FIXME: missing from Athena, check lore
    DAMAGE_AIR_RAW("Air Damage", "%", "FIXME:UNKNOWN", "airDamageBonusRaw"),
    DAMAGE_EARTH_RAW("Earth Damage", "%", "FIXME:UNKNOWN", "earthDamageBonusRaw"),
    DAMAGE_FIRE_RAW("Fire Damage", "%", "FIXME:UNKNOWN", "fireDamageBonusRaw"),
    DAMAGE_THUNDER_RAW("Thunder Damage", "%", "FIXME:UNKNOWN", "thunderDamageBonusRaw"),
    DAMAGE_WATER_RAW("Water Damage", "%", "FIXME:UNKNOWN", "waterDamageBonusRaw"),

    // MAIN ATTACK:
    // rainbow
    DAMAGE_MAIN_ATTACK_ELEMENTAL("FIXME:UNKNOWN", null, "FIXME:UNKNOWN", "mainAttackElementalDamageBonusRaw"),

    // Check lore with e.g. Spearmint or Darting Blur. Missing from Athena
    DAMAGE_MAIN_AIR_RAW("Air Main Attack Damage", null, "FIXME:UNKNOWN", "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH_RAW("Earth Main Attack Damage", null, "FIXME:UNKNOWN", "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE_RAW("Fire Main Attack Damage", null, "FIXME:UNKNOWN", "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER_RAW("Thunder Main Attack Damage", null, "FIXME:UNKNOWN", "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER_RAW("Water Main Attack Damage", null, "FIXME:UNKNOWN", "mainAttackWaterDamageBonusRaw"),

    // FIXME: check lore with Wind Spine
    DAMAGE_MAIN_AIR_PERCENT("Air Main Attack Damage", "%", "FIXME:UNKNOWN", "mainAttackAirDamageBonus"),
    DAMAGE_MAIN_EARTH_PERCENT("Earth Main Attack Damage", "%", "FIXME:UNKNOWN", "mainAttackEarthDamageBonus"),
    DAMAGE_MAIN_FIRE_PERCENT("Fire Main Attack Damage", "%", "FIXME:UNKNOWN", "mainAttackFireDamageBonus"),
    DAMAGE_MAIN_THUNDER_PERCENT("Thunder Main Attack Damage", "%", "FIXME:UNKNOWN", "mainAttackThunderDamageBonus"),
    DAMAGE_MAIN_WATER_PERCENT("Water Main Attack Damage", "%", "FIXME:UNKNOWN", "mainAttackWaterDamageBonus"),

    // OK to here =====
    // SPELL ATTACK / SPELL DAMAGE
    DAMAGE_SPELL_NEUTRAL("Neutral Spell Damage", null, "SPELLNEUTRALDAMAGEBONUSRAW", "spellNeutralDamageBonusRaw"),

    // rainbow
    DAMAGE_SPELL_ELEMENTAL_PERCENT(
            "Elemental Spell Damage", "%", "SPELLELEMENTALDAMAGEBONUS", "spellElementalDamageBonus"),
    DAMAGE_SPELL_ELEMENTAL_RAW(
            "Elemental Spell Damage", null, "SPELLELEMENTALDAMAGEBONUSRAW", "spellElementalDamageBonusRaw"),

    // elemental spell damage
    // FIXME: this is e.g. on Soul Ink. Afaict, Athena is missing this. Check lore name!
    DAMAGE_SPELL_AIR_PERCENT("Air Spell Damage", "%", "FIXME:UNKNOWN", "spellAirDamageBonus"),
    // FIXME: check lore on e.g Decaying Headdress
    DAMAGE_SPELL_EARTH_PERCENT("Earth Spell Damage", "%", "FIXME:UNKNOWN", "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT("Fire Spell Damage", "%", "FIXME:UNKNOWN", "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT("Thunder Spell Damage", "%", "FIXME:UNKNOWN", "spellThunderDamageBonus"),
    // FIXME: check lore on eg Ancient Waters
    DAMAGE_SPELL_WATER_PERCENT("Water Spell Damage", "%", "FIXME:UNKNOWN", "spellWaterDamageBonus"),

    DAMAGE_SPELL_WATER_RAW("Water Spell Damage", null, "SPELLWATERDAMAGEBONUSRAW", "spellWaterDamageBonusRaw"),
    DAMAGE_SPELL_EARTH_RAW("Earth Spell Damage", null, "SPELLEARTHDAMAGEBONUSRAW", "spellEarthDamageBonusRaw"),
    DAMAGE_SPELL_FIRE_RAW("Fire Spell Damage", null, "SPELLFIREDAMAGEBONUSRAW", "spellFireDamageBonusRaw"),
    DAMAGE_SPELL_THUNDER_RAW("Thunder Spell Damage", null, "SPELLTHUNDERDAMAGEBONUSRAW", "spellThunderDamageBonusRaw"),
    DAMAGE_SPELL_AIR_RAW("Air Spell Damage", null, "SPELLAIRDAMAGEBONUSRAW", "spellAirDamageBonusRaw");

    // FROM MISC

    private final String displayName;
    private final String unit;
    private final String loreName;
    private final String apiName;

    GearDamageStat(String displayName, String unit, String loreName, String apiName) {
        this.displayName = displayName;
        this.unit = unit;
        this.loreName = loreName;
        this.apiName = apiName;
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

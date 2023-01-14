/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.google.common.base.CaseFormat;

public enum GearDamageStat implements GearStat {

    // Generate lore name:
    // MAIN_ATTACK_AIR_DAMAGE_BONUS_RAW
    // take api, convert lowerCamelCase to UPPER_CASE_UNDERSCORE.
    // sometimes. But also: SPELLDAMAGERAW

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
    // Lore confirmed OK for these four
    DAMAGE_SPELL_PERCENT("Spell Damage", "%", "spellDamageBonus", "SPELLDAMAGE"),
    DAMAGE_SPELL_RAW("Spell Damage", null, "spellDamageBonusRaw", "SPELLDAMAGERAW"),

    DAMAGE_MAIN_ATTACK_PERCENT("Main Attack Damage", "%", "mainAttackDamageBonus", "DAMAGEBONUS"),
    DAMAGE_MAIN_ATTACK_RAW("Main Attack Damage", null, "mainAttackDamageBonusRaw", "DAMAGEBONUSRAW"),

    // ATTACK TYPE UNSPECIFIED, ELEMENT SPECIFIED

    // rainbow
    DAMAGE_ELEMANTAL_PERCENT("Elemental Damage", "%", "elementalDamageBonus"),
    DAMAGE_ELEMENTAL_RAW("Elemental Damage", null, "elementalDamageBonusRaw"),

    // ELEMENTAL

    // Lore confirmed for these!
    DAMAGE_AIR_PERCENT("Air Damage", "%", "airDamageBonus", "AIRDAMAGEBONUS"),
    DAMAGE_EARTH_PERCENT("Earth Damage", "%", "earthDamageBonus", "EARTHDAMAGEBONUS"),
    DAMAGE_FIRE_PERCENT("Fire Damage", "%", "fireDamageBonus", "FIREDAMAGEBONUS"),
    DAMAGE_THUNDER_PERCENT("Thunder Damage", "%", "thunderDamageBonus", "THUNDERDAMAGEBONUS"),
    DAMAGE_WATER_PERCENT("Water Damage", "%", "waterDamageBonus", "WATERDAMAGEBONUS"),

    DAMAGE_AIR_RAW("Air Damage", "%", "airDamageBonusRaw"),
    DAMAGE_EARTH_RAW("Earth Damage", "%", "earthDamageBonusRaw"),
    DAMAGE_FIRE_RAW("Fire Damage", "%", "fireDamageBonusRaw"),
    DAMAGE_THUNDER_RAW("Thunder Damage", "%", "thunderDamageBonusRaw"),
    DAMAGE_WATER_RAW("Water Damage", "%", "waterDamageBonusRaw"),

    // MAIN ATTACK:
    // rainbow
    DAMAGE_MAIN_ATTACK_ELEMENTAL("Elemental Main Attack Damage", null, "mainAttackElementalDamageBonusRaw"),

    DAMAGE_MAIN_AIR_RAW("Air Main Attack Damage", null, "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH_RAW("Earth Main Attack Damage", null, "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE_RAW("Fire Main Attack Damage", null, "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER_RAW("Thunder Main Attack Damage", null, "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER_RAW("Water Main Attack Damage", null, "mainAttackWaterDamageBonusRaw"),

    DAMAGE_MAIN_AIR_PERCENT("Air Main Attack Damage", "%", "mainAttackAirDamageBonus"),
    DAMAGE_MAIN_EARTH_PERCENT("Earth Main Attack Damage", "%", "mainAttackEarthDamageBonus"),
    DAMAGE_MAIN_FIRE_PERCENT("Fire Main Attack Damage", "%", "mainAttackFireDamageBonus"),
    DAMAGE_MAIN_THUNDER_PERCENT("Thunder Main Attack Damage", "%", "mainAttackThunderDamageBonus"),
    DAMAGE_MAIN_WATER_PERCENT("Water Main Attack Damage", "%", "mainAttackWaterDamageBonus"),

    // OK to here =====
    // SPELL ATTACK / SPELL DAMAGE
    DAMAGE_SPELL_NEUTRAL("Neutral Spell Damage", null, "spellNeutralDamageBonusRaw"),

    // rainbow
    DAMAGE_SPELL_ELEMENTAL_PERCENT("Elemental Spell Damage", "%", "spellElementalDamageBonus"),
    DAMAGE_SPELL_ELEMENTAL_RAW("Elemental Spell Damage", null, "spellElementalDamageBonusRaw"),

    // elemental spell damage
    DAMAGE_SPELL_AIR_PERCENT("Air Spell Damage", "%", "spellAirDamageBonus"),
    DAMAGE_SPELL_EARTH_PERCENT("Earth Spell Damage", "%", "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT("Fire Spell Damage", "%", "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT("Thunder Spell Damage", "%", "spellThunderDamageBonus"),
    DAMAGE_SPELL_WATER_PERCENT("Water Spell Damage", "%", "spellWaterDamageBonus"),

    // FIXME: CHECK LORE
    DAMAGE_SPELL_WATER_RAW("Water Spell Damage", null, "spellWaterDamageBonusRaw", "SPELLWATERDAMAGEBONUSRAW"),
    DAMAGE_SPELL_EARTH_RAW("Earth Spell Damage", null, "spellEarthDamageBonusRaw", "SPELLEARTHDAMAGEBONUSRAW"),
    DAMAGE_SPELL_FIRE_RAW("Fire Spell Damage", null, "spellFireDamageBonusRaw", "SPELLFIREDAMAGEBONUSRAW"),
    DAMAGE_SPELL_THUNDER_RAW("Thunder Spell Damage", null, "spellThunderDamageBonusRaw", "SPELLTHUNDERDAMAGEBONUSRAW"),
    DAMAGE_SPELL_AIR_RAW("Air Spell Damage", null, "spellAirDamageBonusRaw", "SPELLAIRDAMAGEBONUSRAW");

    private final String displayName;
    private final String unit;
    private final String loreName;
    private final String apiName;

    GearDamageStat(String displayName, String unit, String apiName, String loreName) {
        this.displayName = displayName;
        this.unit = unit;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    GearDamageStat(String displayName, String unit, String apiName) {
        this(displayName, unit, apiName, generateLoreName(apiName));
    }

    private static String generateLoreName(String apiName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, apiName);
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

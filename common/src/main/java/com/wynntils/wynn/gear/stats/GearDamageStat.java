/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.google.common.base.CaseFormat;
import com.wynntils.wynn.gear.GearAttackType;
import com.wynntils.wynn.gear.GearDamageType;
import com.wynntils.wynn.gear.GearStatUnit;

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
    DAMAGE_SPELL_PERCENT("Spell Damage",  GearStatUnit.PERCENT, "spellDamageBonus"),
    DAMAGE_SPELL_RAW("Spell Damage", GearStatUnit.RAW, "spellDamageBonusRaw"),

    DAMAGE_MAIN_ATTACK_PERCENT("Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackDamageBonus"),
    DAMAGE_MAIN_ATTACK_RAW("Main Attack Damage",  GearStatUnit.RAW, "mainAttackDamageBonusRaw"),

    // ATTACK TYPE UNSPECIFIED, ELEMENT SPECIFIED

    // rainbow
    DAMAGE_ELEMANTAL_PERCENT("Elemental Damage",  GearStatUnit.PERCENT, "elementalDamageBonus"),
    DAMAGE_ELEMENTAL_RAW("Elemental Damage",  GearStatUnit.RAW, "elementalDamageBonusRaw"),

    // ELEMENTAL

    // Lore confirmed for these!
    DAMAGE_AIR_PERCENT("Air Damage",  GearStatUnit.PERCENT, "airDamageBonus"),
    DAMAGE_EARTH_PERCENT("Earth Damage",  GearStatUnit.PERCENT, "earthDamageBonus"),
    DAMAGE_FIRE_PERCENT("Fire Damage",  GearStatUnit.PERCENT, "fireDamageBonus"),
    DAMAGE_THUNDER_PERCENT("Thunder Damage",  GearStatUnit.PERCENT, "thunderDamageBonus"),
    DAMAGE_WATER_PERCENT("Water Damage",  GearStatUnit.PERCENT, "waterDamageBonus"),

    DAMAGE_AIR_RAW("Air Damage",  GearStatUnit.PERCENT, "airDamageBonusRaw"),
    DAMAGE_EARTH_RAW("Earth Damage",  GearStatUnit.PERCENT, "earthDamageBonusRaw"),
    DAMAGE_FIRE_RAW("Fire Damage",  GearStatUnit.PERCENT, "fireDamageBonusRaw"),
    DAMAGE_THUNDER_RAW("Thunder Damage",  GearStatUnit.PERCENT, "thunderDamageBonusRaw"),
    DAMAGE_WATER_RAW("Water Damage",  GearStatUnit.PERCENT, "waterDamageBonusRaw"),

    // MAIN ATTACK:
    // rainbow
    DAMAGE_MAIN_ATTACK_ELEMENTAL("Elemental Main Attack Damage",  GearStatUnit.RAW, "mainAttackElementalDamageBonusRaw"),

    DAMAGE_MAIN_AIR_RAW("Air Main Attack Damage",  GearStatUnit.RAW, "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH_RAW("Earth Main Attack Damage",  GearStatUnit.RAW, "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE_RAW("Fire Main Attack Damage",  GearStatUnit.RAW, "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER_RAW("Thunder Main Attack Damage",  GearStatUnit.RAW, "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER_RAW("Water Main Attack Damage",  GearStatUnit.RAW, "mainAttackWaterDamageBonusRaw"),

    DAMAGE_MAIN_AIR_PERCENT("Air Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackAirDamageBonus"),
    DAMAGE_MAIN_EARTH_PERCENT("Earth Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackEarthDamageBonus"),
    DAMAGE_MAIN_FIRE_PERCENT("Fire Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackFireDamageBonus"),
    DAMAGE_MAIN_THUNDER_PERCENT("Thunder Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackThunderDamageBonus"),
    DAMAGE_MAIN_WATER_PERCENT("Water Main Attack Damage",  GearStatUnit.PERCENT, "mainAttackWaterDamageBonus"),

    // OK to here =====
    // SPELL ATTACK / SPELL DAMAGE
    DAMAGE_SPELL_NEUTRAL("Neutral Spell Damage",  GearStatUnit.RAW, "spellNeutralDamageBonusRaw"),

    // rainbow
    DAMAGE_SPELL_ELEMENTAL_PERCENT("Elemental Spell Damage",  GearStatUnit.PERCENT, "spellElementalDamageBonus"),
    DAMAGE_SPELL_ELEMENTAL_RAW("Elemental Spell Damage",  GearStatUnit.RAW, "spellElementalDamageBonusRaw"),

    // elemental spell damage
    DAMAGE_SPELL_AIR_PERCENT("Air Spell Damage",  GearStatUnit.PERCENT, "spellAirDamageBonus"),
    DAMAGE_SPELL_EARTH_PERCENT("Earth Spell Damage",  GearStatUnit.PERCENT, "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT("Fire Spell Damage",  GearStatUnit.PERCENT, "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT("Thunder Spell Damage",  GearStatUnit.PERCENT, "spellThunderDamageBonus"),
    DAMAGE_SPELL_WATER_PERCENT("Water Spell Damage",  GearStatUnit.PERCENT, "spellWaterDamageBonus"),

    DAMAGE_SPELL_WATER_RAW("Water Spell Damage",  GearStatUnit.RAW, "spellWaterDamageBonusRaw"),
    DAMAGE_SPELL_EARTH_RAW("Earth Spell Damage",  GearStatUnit.RAW, "spellEarthDamageBonusRaw"),
    DAMAGE_SPELL_FIRE_RAW("Fire Spell Damage",  GearStatUnit.RAW, "spellFireDamageBonusRaw"),
    DAMAGE_SPELL_THUNDER_RAW("Thunder Spell Damage",  GearStatUnit.RAW, "spellThunderDamageBonusRaw"),
    DAMAGE_SPELL_AIR_RAW("Air Spell Damage",  GearStatUnit.RAW, "spellAirDamageBonusRaw");

    private final String displayName;
    private final GearStatUnit unit;
    private final String apiName;

    GearDamageType damageType;
    GearAttackType attackType;

    GearDamageStat(String displayName, GearStatUnit unit, String apiName) {
        this.displayName = displayName;
        this.unit = unit;
        this.apiName = apiName;
    }

    private static String generateLoreName(String apiName) {
        return switch (apiName) {
            // A few damage stats do not follow normal rules
            case "spellDamageBonus" -> "SPELLDAMAGE";
            case "spellDamageBonusRaw" -> "SPELLDAMAGERAW";
            case "mainAttackDamageBonus" -> "DAMAGEBONUS";
            case "mainAttackDamageBonusRaw" -> "DAMAGEBONUSRAW";

            case "airDamageBonus" -> "AIRDAMAGEBONUS";
            case "earthDamageBonus" -> "EARTHDAMAGEBONUS";
            case "fireDamageBonus" -> "FIREDAMAGEBONUS";
            case "thunderDamageBonus" -> "THUNDERDAMAGEBONUS";
            case "waterDamageBonus" -> "WATERDAMAGEBONUS";

            default -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, apiName);
        };
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
    public GearStatUnit getUnit() {
        return unit;
    }

    @Override
    public String getLoreName() {
        return generateLoreName(apiName);
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}

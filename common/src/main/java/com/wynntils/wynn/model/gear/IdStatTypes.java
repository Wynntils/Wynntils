/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public enum IdStatTypes implements IdType {
    SKILL_AGILITY("Agility", null, "rawAgility", "AGILITYPOINTS", "agilityPoints"),
    SKILL_DEFENCE("Defence", null, "rawDefence", "DEFENSEPOINTS", "defensePoints"),
    SKILL_DEXTERITY("Dexterity", null, "rawDexterity", "DEXTERITYPOINTS", "dexterityPoints"),
    SKILL_INTELLIGENCE("Intelligence", null, "rawIntelligence", "INTELLIGENCEPOINTS", "intelligencePoints"),
    SKILL_STRENGTH("Strength", null, "rawStrength", "STRENGTHPOINTS", "strengthPoints"),

    // FIXME: check discrepancy!
    DEFENCE_AIR("Air Defence", "%", "airDefence", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH("Earth Defence", "%", "earthDefence", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE("Fire Defence", "%", "fireDefence", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER("Thunder Defence", "%", "thunderDefence", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER("Water Defence", "%", "waterDefence", "WATERDEFENSE", "bonusWaterDefense"),

    // FIXME: check discrepancy!
    DAMAGE_AIR("Air Damage", "%", "airDamage", "AIRDAMAGEBONUS", "bonusAirDamage"),
    DAMAGE_EARTH("Earth Damage", "%", "earthDamage", "EARTHDAMAGEBONUS", "bonusEarthDamage"),
    DAMAGE_FIRE("Fire Damage", "%", "fireDamage", "FIREDAMAGEBONUS", "bonusFireDamage"),
    DAMAGE_THUNDER("Thunder Damage", "%", "thunderDamage", "THUNDERDAMAGEBONUS", "bonusThunderDamage"),
    DAMAGE_WATER("Water Damage", "%", "waterDamage", "WATERDAMAGEBONUS", "bonusWaterDamage"),

    // FIXME: missing from Athena. Check lore name, e.g. Aleph null.
    // This is variable. How do I know???
    DAMAGE_ELEMANTAL("Elemental Damage", "%", "FIXME:MISSING", "FIXME:UNKNWON", "elementalDamageBonus"),

    // FIXME
    DAMAGE_SPELL_ELEMENTAL_PERCENT("Elemental Spell Damage", "%", "elementalSpellDamage", "SPELLELEMENTALDAMAGEBONUS", "spellElementalDamageBonus"),
    // FIXME, check with only available item: Forest Aconite, untradable
    DAMAGE_SPELL_ELEMENTAL_RAW("Elemental Spell Damage", null, "rawElementalSpellDamage", "FIXME:UNKNOWN", "spellElementalDamageBonusRaw"),

    // FIXME: this is e.g. on Soul Ink. Afaict, Athena is missing this. Check lore name!
    DAMAGE_SPELL_AIR_PERCENT("Air Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellAirDamageBonus"),
    DAMAGE_SPELL_WATER_RAW("Water Spell Damage", null, "FIXME:MISSING", "FIXME:UNKNOWN", "spellWaterDamageBonusRaw"),

    // Note: these are untested, but assumed...
    DAMAGE_SPELL_EARTH_PERCENT("Earth Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellEarthDamageBonus"),
    DAMAGE_SPELL_FIRE_PERCENT("Fire Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellFireDamageBonus"),
    DAMAGE_SPELL_THUNDER_PERCENT("Thunder Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellThunderDamageBonus"),
    DAMAGE_SPELL_WATER_PERCENT("Water Spell Damage", "%", "FIXME:MISSING", "FIXME:UNKNOWN", "spellWaterDamageBonus"),

    DAMAGE_SPELL_EARTH_RAW("Earth Spell Damage", null, "FIXME:MISSING", "FIXME:UNKNOWN", "spellEarthDamageBonusRaw"),
    DAMAGE_SPELL_FIRE_RAW("Fire Spell Damage", null, "FIXME:MISSING", "FIXME:UNKNOWN", "spellFireDamageBonusRaw"),
    DAMAGE_SPELL_THUNDER_RAW("Thunder Spell Damage", null, "FIXME:MISSING", "FIXME:UNKNOWN", "spellThunderDamageBonusRaw"),
    DAMAGE_SPELL_AIR_RAW("Air Spell Damage", null, "FIXME:MISSING", "FIXME:UNKNOWN", "spellAirDamageBonusRaw"),

    // FIXME: should perhaps be named _RAW? Do we have percent? And is the LORE name from old Athena really correct???
    // Check lore with e.g. Spearmint
    DAMAGE_MAIN_AIR("Air Main Attack Damage", null, "rawAirSpellDamage", "SPELLAIRDAMAGEBONUSRAW", "mainAttackAirDamageBonusRaw"),
    DAMAGE_MAIN_EARTH("Earth Main Attack Damage", null, "rawEarthSpellDamage", "SPELLEARTHDAMAGEBONUSRAW", "mainAttackEarthDamageBonusRaw"),
    DAMAGE_MAIN_FIRE("Fire Main Attack Damage", null, "rawFireSpellDamage", "SPELLFIREDAMAGEBONUSRAW", "mainAttackFireDamageBonusRaw"),
    DAMAGE_MAIN_THUNDER("Thunder Main Attack Damage", null, "rawThunderSpellDamage", "SPELLTHUNDERDAMAGEBONUSRAW", "mainAttackThunderDamageBonusRaw"),
    DAMAGE_MAIN_WATER("Water Main Attack Damage", null, "rawWaterSpellDamage", "SPELLWATERDAMAGEBONUSRAW", "mainAttackWaterDamageBonusRaw"),

    // FIXME
    DAMAGE_SPELL_NEUTRAL("Neutral Spell Damage", null, "rawNeutralSpellDamage", "SPELLNEUTRALDAMAGEBONUSRAW", "FIXME"),
    DAMAGE_MAIN_ATTACK_PERCENT("Main Attack Damage", "%", "mainAttackDamage", "DAMAGEBONUS", "damageBonus"),
    DAMAGE_MAIN_ATTACK_RAW("Main Attack Damage", null, "rawMainAttackDamage", "DAMAGEBONUSRAW", "damageBonusRaw"),
    DAMAGE_SPELL_PERCENT("Spell Damage", "%", "spellDamage", "SPELLDAMAGE", "spellDamage"),
    DAMAGE_SPELL_RAW("Spell Damage", null, "rawSpellDamage", "SPELLDAMAGERAW", "spellDamageRaw"),

    HEALTH("Health", null, "rawHealth", "HEALTHBONUS", "healthBonus"),
    HEALTH_REGEN_PERCENT("Health Regen", "%", "healthRegen", "HEALTHREGEN", "healthRegen"),
    HEALTH_REGEN_RAW("Health Regen", null, "rawHealthRegen", "HEALTHREGENRAW", "healthRegenRaw"),
    LIFE_STEAL("Life Steal", "/3s", "lifeSteal", "LIFESTEAL", "lifeSteal"),

    MANA_REGEN("Mana Regen", "/5s", "manaRegen", "MANAREGEN", "manaRegen"),
    MANA_STEAL("Mana Steal", "/3s", "manaSteal", "MANASTEAL", "manaSteal"),

    WALK_SPEED("Walk Speed", "%", "walkSpeed", "SPEED", "speed"),
    // FIXME
    SPRINT("Sprint", "%", "sprint", "STAMINA", "FIXME"),
    // FIXME
    SPRINT_REGEN("Sprint Regen", "%", "sprintRegen", "STAMINA_REGEN", "FIXME"),

    THORNS("Thorns", "%", "thorns", "THORNS", "thorns"),
    EXPLODING("Exploding", "%", "exploding", "EXPLODING", "exploding"),
    POISON("Poison", "/3s", "poison", "POISON", "poison"),
    REFLECTION("Reflection", "%", "reflection", "REFLECTION", "reflection"),

    STEALING("Stealing", "%", "stealing", "EMERALDSTEALING", "emeraldStealing"),
    // FIXME: Check discrepancy between LORE and api
    ATTACK_SPEED("Attack Speed", " tier", "attackSpeed", "ATTACKSPEED", "attackSpeedBonus"),
    // FIXME
    JUMP_HEIGHT("Jump Height", null, "rawJumpHeight", "JUMP_HEIGHT", "FIXME"),
    LOOT_BONUS("Loot Bonus", "%", "lootBonus", "LOOTBONUS", "lootBonus"),
    SOUL_POINT_REGEN("Soul Point Regen", "%", "soulPointRegen", "SOULPOINTS", "soulPoints"),
    XP_BONUS("XP Bonus", "%", "xpBonus", "XPBONUS", "xpBonus");

    /*
    "SPELLWATERDAMAGEBONUSRAW" -> "rawWaterSpellDamage"
    "SPELLFIREDAMAGEBONUSRAW" -> "rawFireSpellDamage"
    "SPELLTHUNDERDAMAGEBONUSRAW" -> "rawThunderSpellDamage"
    "SPELLAIRDAMAGEBONUSRAW" -> "rawAirSpellDamage"
    "SPELLEARTHDAMAGEBONUSRAW" -> "rawEarthSpellDamage"

    "SPELLELEMENTALDAMAGEBONUSRAW" -> "rawElementalSpellDamage"

    "SPELL_COST_RAW_1" -> "raw1stSpellCost"
    "SPELL_COST_RAW_2" -> "raw2ndSpellCost"
    "SPELL_COST_RAW_3" -> "raw3rdSpellCost"
    "SPELL_COST_RAW_4" -> "raw4thSpellCost"
    "SPELL_COST_PCT_1" -> "1stSpellCost"
    "SPELL_COST_PCT_2" -> "2ndSpellCost"
    "SPELL_COST_PCT_3" -> "3rdSpellCost"
    "SPELL_COST_PCT_4" -> "4thSpellCost"
     */

    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    IdStatTypes(String displayName, String unit, String athenaName, String loreName, String apiName) {
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

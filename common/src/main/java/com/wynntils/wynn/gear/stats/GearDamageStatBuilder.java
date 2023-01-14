/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.google.common.base.CaseFormat;
import com.wynntils.wynn.gear.GearAttackType;
import com.wynntils.wynn.gear.GearDamageType;
import com.wynntils.wynn.gear.GearStatUnit;
import java.util.List;

public class GearDamageStatBuilder implements GearStat {
    public GearDamageStatBuilder(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        this.attackType = attackType;
        this.damageType = damageType;
        this.unit = unit;

        this.apiName = buildApiName(attackType, damageType, unit);
        this.displayName = buildDisplayName(attackType, damageType);
        this.key = buildKey(attackType, damageType, unit);
    }


    private String buildApiName(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL,
                attackType.getApiName() + damageType.getApiName() + (unit == GearStatUnit.RAW ? "Raw" : ""));
    }

    private String buildKey(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        return "DAMAGE_" + attackType.name() + "_" + damageType.name() + "_" + unit.name();
    }

    private String buildDisplayName(GearAttackType attackType, GearDamageType damageType) {
        return damageType.getDisplayName() + attackType.getDisplayName() + "Damage";
    }

    public static void addStats(List<GearStat> registry) {
        for (GearAttackType attackType : GearAttackType.values()) {
            for (GearDamageType damageType : GearDamageType.values()) {
                GearDamageStatBuilder rawType = new GearDamageStatBuilder(attackType, damageType, GearStatUnit.RAW);
                registry.add(rawType);
                GearDamageStatBuilder percentType = new GearDamageStatBuilder(attackType, damageType, GearStatUnit.PERCENT);
                registry.add(percentType);
            }
        }
    }

    GearAttackType attackType;
    GearDamageType damageType;
    private final GearStatUnit unit;

    private final String displayName;
    private final String apiName;
    private final String key;

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
        return key;
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

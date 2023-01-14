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

public class GearDamageStatBuilder {
    public static void addStats(List<GearStat> registry) {
        for (GearAttackType attackType : GearAttackType.values()) {
            for (GearDamageType damageType : GearDamageType.values()) {
                GearStatHolder rawType = buildDamageStat(attackType, damageType, GearStatUnit.RAW);
                registry.add(rawType);

                GearStatHolder percentType = buildDamageStat(attackType, damageType, GearStatUnit.PERCENT);
                registry.add(percentType);
            }
        }
    }

    private static GearStatHolder buildDamageStat(
            GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        GearStatHolder rawType;
        String apiName = buildApiName(attackType, damageType, unit);
        rawType = new GearStatHolder(
                buildKey(attackType, damageType, unit),
                buildDisplayName(attackType, damageType),
                apiName,
                buildLoreName(apiName),
                unit);
        return rawType;
    }

    private static String buildApiName(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL,
                attackType.getApiName() + damageType.getApiName() + (unit == GearStatUnit.RAW ? "Raw" : ""));
    }

    private static String buildKey(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        return "DAMAGE_" + attackType.name() + "_" + damageType.name() + "_" + unit.name();
    }

    private static String buildDisplayName(GearAttackType attackType, GearDamageType damageType) {
        return damageType.getDisplayName() + attackType.getDisplayName() + "Damage";
    }

    private static String buildLoreName(String apiName) {
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
}

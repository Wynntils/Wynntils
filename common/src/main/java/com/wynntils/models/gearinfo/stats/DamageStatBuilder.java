/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.stats;

import com.google.common.base.CaseFormat;
import com.wynntils.models.gearinfo.types.GearAttackType;
import com.wynntils.models.gearinfo.types.GearDamageType;
import com.wynntils.models.gearinfo.types.GearStat;
import com.wynntils.models.gearinfo.types.GearStatUnit;
import java.util.function.Consumer;

public final class DamageStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<GearStat> callback) {
        for (GearAttackType attackType : GearAttackType.values()) {
            for (GearDamageType damageType : GearDamageType.values()) {
                GearStat rawType = buildDamageStat(attackType, damageType, GearStatUnit.RAW);
                callback.accept(rawType);

                GearStat percentType = buildDamageStat(attackType, damageType, GearStatUnit.PERCENT);
                callback.accept(percentType);
            }
        }
    }

    private static GearStat buildDamageStat(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        String apiName = buildApiName(attackType, damageType, unit);
        return new GearStat(
                buildKey(attackType, damageType, unit),
                buildDisplayName(attackType, damageType),
                apiName,
                buildLoreName(apiName),
                unit);
    }

    private static String buildApiName(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL,
                attackType.getApiName() + damageType.getApiName() + "DamageBonus"
                        + (unit == GearStatUnit.RAW ? "Raw" : ""));
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

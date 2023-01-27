/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.google.common.base.CaseFormat;
import com.wynntils.models.gearinfo.type.GearAttackType;
import com.wynntils.models.gearinfo.type.GearDamageType;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.function.Consumer;

public final class DamageStatBuilder extends StatBuilder {
    @Override
    public void buildStats(Consumer<StatType> callback) {
        for (GearAttackType attackType : GearAttackType.values()) {
            for (GearDamageType damageType : GearDamageType.values()) {
                StatType rawType = buildDamageStat(attackType, damageType, StatUnit.RAW);
                callback.accept(rawType);

                StatType percentType = buildDamageStat(attackType, damageType, StatUnit.PERCENT);
                callback.accept(percentType);
            }
        }
    }

    private static StatType buildDamageStat(GearAttackType attackType, GearDamageType damageType, StatUnit unit) {
        String apiName = buildApiName(attackType, damageType, unit);
        return new DamageStatType(
                buildKey(attackType, damageType, unit),
                buildDisplayName(attackType, damageType),
                apiName,
                buildLoreName(apiName),
                unit);
    }

    private static String buildApiName(GearAttackType attackType, GearDamageType damageType, StatUnit unit) {
        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL,
                attackType.getApiName() + damageType.getApiName() + "DamageBonus"
                        + (unit == StatUnit.RAW ? "Raw" : ""));
    }

    private static String buildKey(GearAttackType attackType, GearDamageType damageType, StatUnit unit) {
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

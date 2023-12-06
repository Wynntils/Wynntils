/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.google.common.base.CaseFormat;
import com.wynntils.models.stats.type.AttackType;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.StringUtils;
import java.util.function.Consumer;

public final class DamageStatBuilder extends StatBuilder<DamageStatType> {
    @Override
    public void buildStats(Consumer<DamageStatType> callback) {
        for (AttackType attackType : AttackType.values()) {
            for (DamageType damageType : DamageType.statValues()) {
                DamageStatType percentType = buildDamageStat(attackType, damageType, StatUnit.PERCENT);
                callback.accept(percentType);

                DamageStatType rawType = buildDamageStat(attackType, damageType, StatUnit.RAW);
                callback.accept(rawType);
            }
        }
    }

    private static DamageStatType buildDamageStat(AttackType attackType, DamageType damageType, StatUnit unit) {
        return new DamageStatType(
                buildKey(attackType, damageType, unit),
                buildDisplayName(attackType, damageType),
                buildApiName(attackType, damageType, unit),
                buildInternalRollName(buildOldApiName(attackType, damageType, unit)),
                unit);
    }

    // This format is still used for internal rolls, but not for the api
    private static String buildOldApiName(AttackType attackType, DamageType damageType, StatUnit unit) {
        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL,
                attackType.getApiName() + damageType.getApiName() + "DamageBonus"
                        + (unit == StatUnit.RAW ? "Raw" : ""));
    }

    private static String buildApiName(AttackType attackType, DamageType damageType, StatUnit unit) {
        if (unit == StatUnit.RAW) {
            return "raw" + StringUtils.capitalizeFirst(damageType.getApiName()) + attackType.getApiName() + "Damage";
        }

        return CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, damageType.getApiName() + attackType.getApiName() + "Damage");
    }

    private static String buildKey(AttackType attackType, DamageType damageType, StatUnit unit) {
        return "DAMAGE_" + attackType.name() + "_" + damageType.name() + "_" + unit.name();
    }

    private static String buildDisplayName(AttackType attackType, DamageType damageType) {
        return damageType.getDisplayName() + attackType.getDisplayName() + "Damage";
    }

    private static String buildInternalRollName(String apiName) {
        return switch (apiName) {
                // A few damage stats do not follow normal rules, but instead has legacy names
                // This list comes from Wynncraft internals, courtesy of HeyZeer0
            case "spellDamageBonus" -> "SPELLDAMAGE";
            case "spellDamageBonusRaw" -> "SPELLDAMAGERAW";
            case "mainAttackDamageBonus" -> "DAMAGEBONUS";
            case "mainAttackDamageBonusRaw" -> "DAMAGEBONUSRAW";
            case "spellElementalDamageBonusRaw" -> "RAINBOWSPELLDAMAGERAW";
            case "fireDamageBonus" -> "FIREDAMAGEBONUS";
            case "waterDamageBonus" -> "WATERDAMAGEBONUS";
            case "airDamageBonus" -> "AIRDAMAGEBONUS";
            case "thunderDamageBonus" -> "THUNDERDAMAGEBONUS";
            case "earthDamageBonus" -> "EARTHDAMAGEBONUS";

            default -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, apiName);
        };
    }
}

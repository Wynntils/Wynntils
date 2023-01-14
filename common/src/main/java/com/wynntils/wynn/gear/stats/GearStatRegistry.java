/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.google.common.base.CaseFormat;
import com.wynntils.wynn.gear.GearAttackType;
import com.wynntils.wynn.gear.GearDamageType;
import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.Element;
import com.wynntils.wynn.objects.SpellType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GearStatRegistry {
    private static final List<StatBuilder> BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());
    public static final List<GearStat> registry = new ArrayList<>();

    static {
        for (StatBuilder builder : BUILDERS) {
            builder.processStats(registry::add);
        }
    }

    public abstract static class StatBuilder {
        public abstract void processStats(Consumer<GearStat> callback);
    }

    public static final class MiscStatBuilder extends StatBuilder {
        @Override
        public void processStats(Consumer<GearStat> callback) {
            for (GearMiscStatType statType : GearMiscStatType.values()) {
                GearStat gearStat = new GearStat(
                        "MISC_" + statType.name(),
                        statType.getDisplayName(),
                        statType.getApiName(),
                        statType.getLoreName(),
                        statType.getUnit());
                callback.accept(gearStat);
            }
        }
    }

    public static final class DefenceStatBuilder extends StatBuilder {
        @Override
        public void processStats(Consumer<GearStat> callback) {
            for (Element element : Element.values()) {
                // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
                String displayName = element.getDisplayName() + " Defence";
                String apiName = "bonus" + element.getDisplayName() + "Defense";
                String loreName = element.name() + "DEFENSE";
                String key = "DEFENCE_" + element.name();

                GearStat gearStat = new GearStat(key, displayName, apiName, loreName, GearStatUnit.PERCENT);
                callback.accept(gearStat);
            }
        }
    }

    public static final class SpellStatBuilder extends StatBuilder {
        @Override
        public void processStats(Consumer<GearStat> callback) {
            for (var spellType : SpellType.values()) {
                int spellNumber = spellType.getSpellNumber();
                String displayName = spellType.getName() + " Cost";

                GearStat percentType = new GearStat(
                        "SPELL_" + spellType.name() + "_COST_PERCENT",
                        displayName,
                        "spellCostPct" + spellNumber,
                        "SPELL_COST_PCT_" + spellNumber,
                        GearStatUnit.PERCENT);
                callback.accept(percentType);

                GearStat rawType = new GearStat(
                        "SPELL_" + spellType.name() + "_COST_RAW",
                        displayName,
                        "spellCostRaw" + spellNumber,
                        "SPELL_COST_RAW_" + spellNumber,
                        GearStatUnit.RAW);
                callback.accept(rawType);
                if (spellType.getClassType() == ClassType.None) {
                    // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                    GearStat rawTypeAlias = new GearStat(
                            spellType.name() + "_COST_RAW_ALIAS",
                            "{sp" + spellNumber + "} Cost",
                            "spellCostRaw" + spellNumber,
                            "SPELL_COST_RAW_" + spellNumber,
                            null);
                    callback.accept(rawTypeAlias);
                }
            }
        }
    }

    public static final class DamageStatBuilder extends StatBuilder {
        @Override
        public void processStats(Consumer<GearStat> callback) {
            for (GearAttackType attackType : GearAttackType.values()) {
                for (GearDamageType damageType : GearDamageType.values()) {
                    GearStat rawType = buildDamageStat(attackType, damageType, GearStatUnit.RAW);
                    callback.accept(rawType);

                    GearStat percentType = buildDamageStat(attackType, damageType, GearStatUnit.PERCENT);
                    callback.accept(percentType);
                }
            }
        }

        private static GearStat buildDamageStat(
                GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
            GearStat rawType;
            String apiName = buildApiName(attackType, damageType, unit);
            rawType = new GearStat(
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
}

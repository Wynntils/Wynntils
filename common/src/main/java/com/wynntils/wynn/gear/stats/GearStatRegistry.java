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

public class GearStatRegistry {
    public static final List<GearStat> registry = new ArrayList<>();

    static {
        GearDamageStatBuilder.addStats(registry);
        GearDefenceStatBuilder.addStats(registry);
        GearSpellStatBuilder.addStats(registry);
        GearMiscStatBuilder.addStats(registry);
    }

    public static class GearDamageStatBuilder {
        public static void addStats(List<GearStat> registry) {
            for (GearAttackType attackType : GearAttackType.values()) {
                for (GearDamageType damageType : GearDamageType.values()) {
                    GearStat rawType = buildDamageStat(attackType, damageType, GearStatUnit.RAW);
                    registry.add(rawType);

                    GearStat percentType = buildDamageStat(attackType, damageType, GearStatUnit.PERCENT);
                    registry.add(percentType);
                }
            }
        }

        private static GearStat buildDamageStat(GearAttackType attackType, GearDamageType damageType, GearStatUnit unit) {
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

    public static class GearSpellStatBuilder {
        public static void addStats(List<GearStat> registry) {
            for (var spellType : SpellType.values()) {
                int spellNumber = spellType.getSpellNumber();
                String ordinal =
                        switch (spellNumber) {
                            case 1 -> "1st";
                            case 2 -> "2nd";
                            case 3 -> "3rd";
                            case 4 -> "4th";
                            default -> throw new IllegalStateException("Bad SpellType");
                        };
                String displayName = spellType.getName() + " Cost";

                GearSpellStat percentType = new GearSpellStat(
                        spellType,
                        "SPELL_" + spellType.name() + "_COST_PERCENT",
                        displayName,
                        "spellCostPct" + spellNumber,
                        "SPELL_COST_PCT_" + spellNumber,
                        GearStatUnit.PERCENT);
                registry.add(percentType);
                GearSpellStat rawType = new GearSpellStat(
                        spellType,
                        "SPELL_" + spellType.name() + "_COST_RAW",
                        displayName,
                        "spellCostRaw" + spellNumber,
                        "SPELL_COST_RAW_" + spellNumber,
                        GearStatUnit.RAW);
                registry.add(rawType);
                if (spellType.getClassType() == ClassType.None) {
                    // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                    GearSpellStat rawTypeAlias = new GearSpellStat(
                            spellType,
                            spellType.name() + "_COST_RAW_ALIAS",
                            "{sp" + spellNumber + "} Cost",
                            "spellCostRaw" + spellNumber,
                            "SPELL_COST_RAW_" + spellNumber,
                            null);
                    registry.add(rawTypeAlias);
                }
            }
        }
    }

    public static class GearMiscStatBuilder {
        public static void addStats(List<GearStat> registry) {
            for (GearMiscStatType miscStat : GearMiscStatType.values()) {
                GearStat holder = new GearStat(
                        "MISC_" + miscStat.name(),
                        miscStat.getDisplayName(),
                        miscStat.getApiName(),
                        miscStat.getLoreName(),
                        miscStat.getUnit());
                registry.add(holder);
            }
        }
    }

    public static class GearDefenceStatBuilder {
        public static void addStats(List<GearStat> registry) {
            for (Element element : Element.values()) {
                // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
                String displayName = element.getDisplayName() + " Defence";
                String apiName = "bonus" + element.getDisplayName() + "Defense";
                String loreName = element.name() + "DEFENSE";
                String key = "DEFENCE_" + element.name();
                GearStat rawType = new GearStat(key, displayName, apiName, loreName, GearStatUnit.PERCENT);
                registry.add(rawType);
            }
        }
    }
}

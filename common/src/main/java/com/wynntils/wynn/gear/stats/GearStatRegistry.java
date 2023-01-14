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
    private static final List<StatBuilder> BUILDERS = List.of(
            new GearMiscStatBuilder(),
            new GearDefenceStatBuilder(),
            new GearSpellStatBuilder(),
            new GearDamageStatBuilder());
    public static final List<GearStat> registry = new ArrayList<>();

    static {
        for (StatBuilder builder : BUILDERS) {
            builder.addStats(registry);
        }
    }

    public abstract static class StatBuilder {
        public abstract void addStats(List<GearStat> registry);
    }

    public static final class GearMiscStatBuilder extends StatBuilder {
        public void addStats(List<GearStat> registry) {
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

    public static final class GearDefenceStatBuilder extends StatBuilder {
        public void addStats(List<GearStat> registry) {
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

    public static final class GearSpellStatBuilder extends StatBuilder {
        public void addStats(List<GearStat> registry) {
            for (var spellType : SpellType.values()) {
                int spellNumber = spellType.getSpellNumber();
                String displayName = spellType.getName() + " Cost";

                GearStat percentType = new GearStat(
                        "SPELL_" + spellType.name() + "_COST_PERCENT",
                        displayName,
                        "spellCostPct" + spellNumber,
                        "SPELL_COST_PCT_" + spellNumber,
                        GearStatUnit.PERCENT);
                registry.add(percentType);
                GearStat rawType = new GearStat(
                        "SPELL_" + spellType.name() + "_COST_RAW",
                        displayName,
                        "spellCostRaw" + spellNumber,
                        "SPELL_COST_RAW_" + spellNumber,
                        GearStatUnit.RAW);
                registry.add(rawType);
                if (spellType.getClassType() == ClassType.None) {
                    // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                    GearStat rawTypeAlias = new GearStat(
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

    public static final class GearDamageStatBuilder extends StatBuilder {
        public void addStats(List<GearStat> registry) {
            for (GearAttackType attackType : GearAttackType.values()) {
                for (GearDamageType damageType : GearDamageType.values()) {
                    GearStat rawType = buildDamageStat(attackType, damageType, GearStatUnit.RAW);
                    registry.add(rawType);

                    GearStat percentType = buildDamageStat(attackType, damageType, GearStatUnit.PERCENT);
                    registry.add(percentType);
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

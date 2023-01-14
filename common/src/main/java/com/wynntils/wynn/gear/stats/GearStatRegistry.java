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
import java.util.Locale;
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

    public static GearStat getIdType(String idName, String unit) {
        // FIXME: Faster lookup
        for (GearStat statType : registry) {
            if (statType.displayName().equals(idName)
                    && statType.unit().getDisplayName().equals(unit)) {
                return statType;
            }
        }

        return null;
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
                GearStat gearStat = new GearStat(
                        "DEFENCE_" + element.name(),
                        element.getDisplayName() + " Defence",
                        "bonus" + element.getDisplayName() + "Defense",
                        element.name() + "DEFENSE",
                        GearStatUnit.PERCENT);
                callback.accept(gearStat);
            }
        }
    }

    public static final class SpellStatBuilder extends StatBuilder {
        @Override
        public void processStats(Consumer<GearStat> callback) {
            for (SpellType spellType : SpellType.values()) {
                int spellNumber = spellType.getSpellNumber();
                String displayName = spellType.getName() + " Cost";

                GearStat percentType = buildSpellStat(spellType, spellNumber, displayName, GearStatUnit.PERCENT, "");
                callback.accept(percentType);

                GearStat rawType = buildSpellStat(spellType, spellNumber, displayName, GearStatUnit.RAW, "");
                callback.accept(rawType);

                if (spellType.getClassType() == ClassType.None) {
                    // Also add an alias of the form "{sp1} Cost" which can appear on Unidentified gear
                    String aliasName = "{sp" + spellNumber + "} Cost";
                    GearStat percentTypeAlias =
                            buildSpellStat(spellType, spellNumber, aliasName, GearStatUnit.PERCENT, "_ALIAS");
                    callback.accept(percentTypeAlias);

                    GearStat rawTypeAlias =
                            buildSpellStat(spellType, spellNumber, aliasName, GearStatUnit.RAW, "_ALIAS");
                    callback.accept(rawTypeAlias);
                }
            }
        }

        private GearStat buildSpellStat(
                SpellType spellType, int spellNumber, String displayName, GearStatUnit unit, String postfix) {
            String apiUnit = (unit == GearStatUnit.RAW) ? "Raw" : "Pct";
            String loreUnit = apiUnit.toUpperCase(Locale.ROOT);

            return new GearStat(
                    "SPELL_" + spellType.name() + "_COST_" + unit.name() + postfix,
                    displayName,
                    "spellCost" + apiUnit + spellNumber,
                    "SPELL_COST_" + loreUnit + "_" + spellNumber,
                    unit);
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

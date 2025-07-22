/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.player.LocalPlayer;

public class CharacterFunctions {
    public static class CappedManaFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY);
        }
    }

    public static class CappedHealthFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY);
        }
    }

    public static class SprintFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getSprint().orElse(CappedValue.EMPTY);
        }
    }

    public static class BpsFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            double dY = player.getY() - player.yOld;
            return Math.sqrt((dX * dX) + (dZ * dZ) + (dY * dY)) * 20;
        }
    }

    public static class BpsXzFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            return Math.sqrt((dX * dX) + (dZ * dZ)) * 20;
        }
    }

    public static class StatusEffectsFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            List<String> statusEffectsList = Models.StatusEffect.getStatusEffects().stream()
                    .map(statusEffect -> statusEffect.asString().getString())
                    .toList();

            return String.join("\n", statusEffectsList);
        }
    }

    public static class StatusEffectActiveFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            String query = arguments.getArgument("query").getStringValue();
            return Models.StatusEffect.getStatusEffects().stream()
                    .anyMatch(statusEffect ->
                            statusEffect.getName().getStringWithoutFormatting().equals(query));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("contains_effect");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("query", String.class, null)));
        }
    }

    public static class StatusEffectDurationFunction extends Function<NamedValue> {
        @Override
        public NamedValue getValue(FunctionArguments arguments) {
            String query = arguments.getArgument("query").getStringValue();
            StatusEffect effect = Models.StatusEffect.searchStatusEffectByName(query);
            if (effect == null) return NamedValue.EMPTY;
            return new NamedValue(effect.getName().getString(), effect.getDuration());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("query", String.class, null)));
        }
    }

    public static class StatusEffectModifierFunction extends Function<NamedValue> {
        @Override
        public NamedValue getValue(FunctionArguments arguments) {
            String query = arguments.getArgument("query").getStringValue();
            StatusEffect effect = Models.StatusEffect.searchStatusEffectByName(query);
            if (effect == null || !effect.hasModifierValue()) return NamedValue.EMPTY;
            return new NamedValue(effect.getModifierSuffix().getString(), effect.getModifierValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("query", String.class, null)));
        }
    }

    public static class ClassFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Boolean showReskinnedName =
                    arguments.getArgument("showReskinnedName").getBooleanValue();

            String name = showReskinnedName
                    ? Models.Character.getActualName()
                    : Models.Character.getClassType().getActualName(false);

            if (arguments.getArgument("uppercase").getBooleanValue()) {
                return name.toUpperCase(Locale.ROOT);
            }

            return name;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("uppercase", Boolean.class, false),
                    new FunctionArguments.Argument<>("showReskinnedName", Boolean.class, true)));
        }
    }

    public static class ManaFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).current();
        }
    }

    public static class ManaMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).max();
        }
    }

    public static class HealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).current();
        }
    }

    public static class HealthMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).max();
        }
    }

    public static class HealthPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).getPercentage();
        }
    }

    public static class ManaPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).getPercentage();
        }
    }

    public static class IdFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Character.getId();
        }
    }

    public static class CappedAwakenedProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.awakenedBar.isActive()
                    ? Models.Ability.awakenedBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedBloodPoolFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.bloodPoolBar.isActive()
                    ? Models.Ability.bloodPoolBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedCorruptedFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.corruptedBar.isActive()
                    ? Models.Ability.corruptedBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedFocusFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.focusBar.isActive()
                    ? Models.Ability.focusBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedManaBankFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.manaBankBar.isActive()
                    ? Models.Ability.manaBankBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedOphanimFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.ophanimBar.isActive()
                    ? Models.Ability.ophanimBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class OphanimOrb extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int orbNumber = arguments.getArgument("orbNumber").getIntegerValue();
            return orbNumber < Models.Ability.ophanimBar.getOrbs().size() && orbNumber >= 0
                    ? Models.Ability.ophanimBar.getOrbs().get(orbNumber).getHealthState()
                    : -1;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("orbNumber", Integer.class, null)));
        }
    }

    public static class OphanimActive extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Ability.ophanimBar.isActive();
        }
    }

    public static class CappedHolyPowerFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.holyPowerBar.isActive()
                    ? Models.Ability.holyPowerBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            // Old function name before ability rename, keep to not break old functions
            return List.of("capped_sacred_surge");
        }
    }

    public static class CommanderDurationFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Ability.commanderBar.isActive() ? Models.Ability.commanderBar.getDuration() : 0;
        }
    }

    public static class CommanderActivatedFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Ability.commanderBar.isActive() && Models.Ability.commanderBar.isActivated();
        }
    }
}

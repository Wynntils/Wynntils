/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.type.NamedValue;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public class StatusEffectFunctions {
    private abstract static class StatusEffectFunctionBase<T> extends Function<T> {
        @Override
        public T getValue(FunctionArguments arguments) {
            String query = arguments.getArgument("query").getStringValue();
            StatusEffect effect = Models.StatusEffect.searchStatusEffectByName(query);
            if (effect == null) return whenNotFound();
            return processEffect(effect);
        }

        @Override
        public String getArgumentDescription(String argumentName) {
            return I18n.get("function.wynntils.statusEffectFunctionBase.argument." + argumentName);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("query", String.class, null)));
        }

        public abstract T processEffect(StatusEffect effect);

        public abstract T whenNotFound();
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
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("query", String.class, null)));
        }
    }

    public static class StatusEffectDurationFunction extends StatusEffectFunctionBase<NamedValue> {
        @Override
        public NamedValue processEffect(StatusEffect effect) {
            return new NamedValue(effect.getName().getString(), effect.getDuration());
        }

        @Override
        public NamedValue whenNotFound() {
            return NamedValue.EMPTY;
        }
    }

    public static class StatusEffectModifierFunction extends StatusEffectFunctionBase<NamedValue> {
        @Override
        public NamedValue processEffect(StatusEffect effect) {
            if (!effect.hasModifierValue()) return whenNotFound();
            return new NamedValue(effect.getModifierSuffix().getString(), effect.getModifierValue());
        }

        @Override
        public NamedValue whenNotFound() {
            return NamedValue.EMPTY;
        }
    }

    public static class StatusEffectPrefixFunction extends StatusEffectFunctionBase<String> {
        @Override
        public String processEffect(StatusEffect effect) {
            return effect.getPrefix().getString();
        }

        @Override
        public String whenNotFound() {
            return "";
        }
    }
}

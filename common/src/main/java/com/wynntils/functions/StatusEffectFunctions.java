/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.NamedValue;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class StatusEffectFunctions {

    private static Optional<StatusEffect> statusEffectFunctionBase(String query) {
        return Optional.ofNullable(Models.StatusEffect.searchStatusEffectByName(query));
    }

    @TemplateFunction(name = "status_effects")
    public static String statusEffectsFunction() {
        List<String> statusEffectsList = Models.StatusEffect.getStatusEffects().stream()
                .map(statusEffect -> statusEffect.asString().getString())
                .toList();

        return String.join("\n", statusEffectsList);
    }

    @TemplateFunction(name = "status_effect_active", aliases = {"contains_effect"})
    public static boolean statusEffectActiveFunction(String query) {
        return Models.StatusEffect.getStatusEffects().stream()
                .anyMatch(statusEffect ->
                        statusEffect.getName().getStringWithoutFormatting().equals(query));
    }

    @TemplateFunction(name = "status_effect_duration")
    public static NamedValue statusEffectDurationFunction(String query) {
        return statusEffectFunctionBase(query)
                .map(effect -> new NamedValue(effect.getName().getString(), effect.getDuration()))
                .orElse(NamedValue.EMPTY);
    }

    @TemplateFunction(name = "status_effect_modifier")
    public static NamedValue statusEffectModiferFunction(String query) {
        return statusEffectFunctionBase(query)
                .map(effect -> effect.hasModifierValue()
                        ? new NamedValue(effect.getModifierSuffix().getString(), effect.getModifierValue())
                        : NamedValue.EMPTY)
                .orElse(NamedValue.EMPTY);
    }

    @TemplateFunction(name = "status_effect_prefix")
    public static String statusEffectPrefixFunction(String query) {
        return statusEffectFunctionBase(query)
                .map(effect -> effect.getPrefix().getString())
                .orElse("");
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Map;
import java.util.Set;

public class QuickCastTimingsToMillisecondsUpfixer implements Upfixer {
    private static final Map<String, String> TIMING_KEYS = Map.of(
            "quickCastFeature.leftClickTickDelay", "quickCastFeature.leftClickDelayMs",
            "quickCastFeature.rightClickTickDelay", "quickCastFeature.rightClickDelayMs",
            "quickCastFeature.spellCooldown", "quickCastFeature.spellCooldownMs");

    private static final Set<String> REMOVED_KEYS =
            Set.of("quickCastFeature.blockAttacks", "quickCastFeature.safeCasting");

    private static final int MILLIS_PER_TICK = 50;

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        boolean changed = false;

        for (Map.Entry<String, String> entry : TIMING_KEYS.entrySet()) {
            JsonElement oldValue = configObject.get(entry.getKey());
            if (oldValue == null) continue;

            if (!configObject.has(entry.getValue())) {
                configObject.addProperty(entry.getValue(), oldValue.getAsInt() * MILLIS_PER_TICK);
            }

            configObject.remove(entry.getKey());
            changed = true;
        }

        for (String removedKey : REMOVED_KEYS) {
            if (configObject.remove(removedKey) != null) {
                changed = true;
            }
        }

        return changed;
    }
}

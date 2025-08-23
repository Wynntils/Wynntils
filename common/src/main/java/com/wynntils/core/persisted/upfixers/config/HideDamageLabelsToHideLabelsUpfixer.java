/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Set;

public class HideDamageLabelsToHideLabelsUpfixer implements Upfixer {
    private static final String HIDE_DAMAGE_LABELS_FEATURE_USER_ENABLED = "hideDamageLabelsFeature.userEnabled";
    private static final String HIDE_LABELS_FEATURE_USER_ENABLED = "hideLabelsFeature.userEnabled";
    private static final String HIDE_LABELS_DAMAGE_CONFIG = "hideLabelsFeature.hideDamageLabels";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(HIDE_DAMAGE_LABELS_FEATURE_USER_ENABLED)) {
            JsonPrimitive configValue = configObject.getAsJsonPrimitive(HIDE_DAMAGE_LABELS_FEATURE_USER_ENABLED);

            if (!configValue.isBoolean()) return true;

            boolean configValueBoolean = configValue.getAsBoolean();
            configObject.addProperty(HIDE_LABELS_FEATURE_USER_ENABLED, configValueBoolean);

            if (configValueBoolean) {
                configObject.addProperty(HIDE_LABELS_DAMAGE_CONFIG, true);
            }
        }

        return true;
    }
}

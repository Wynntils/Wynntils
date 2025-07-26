/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.type.ShiftBehavior;
import java.util.Set;

public class WynntilsContentBookReplaceToShiftBehaviourUpfixer implements Upfixer {
    private static final String CONTENT_BOOK_REPLACE_OBJECT_NAME =
            "wynntilsContentBookFeature.replaceWynncraftContentBook";
    private static final String CONTENT_BOOK_SHIFT_OBJECT_NAME = "wynntilsContentBookFeature.shiftBehaviorConfig";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(CONTENT_BOOK_REPLACE_OBJECT_NAME)) {
            JsonPrimitive configValue = configObject.getAsJsonPrimitive(CONTENT_BOOK_REPLACE_OBJECT_NAME);

            if (!configValue.isBoolean()) return true;

            boolean configValueBoolean = configValue.getAsBoolean();

            if (configValueBoolean) {
                configObject.addProperty(
                        CONTENT_BOOK_SHIFT_OBJECT_NAME, EnumUtils.toJsonFormat(ShiftBehavior.DISABLED_IF_SHIFT_HELD));
            } else {
                configObject.addProperty(
                        CONTENT_BOOK_SHIFT_OBJECT_NAME, EnumUtils.toJsonFormat(ShiftBehavior.ENABLED_IF_SHIFT_HELD));
            }
        }

        return true;
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.handlers.tooltip.type.TooltipOptions.RollStyle;
import com.wynntils.utils.EnumUtils;
import java.util.Set;

public class ItemStatInfoRollStyleUpfixer implements Upfixer {
    private static final String PREFIX = "itemStatInfoFeature.";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        JsonElement overallPercentage = configObject.remove(PREFIX + "overallPercentageInPerfectDefectiveName");
        if (overallPercentage != null && !configObject.has(PREFIX + "overallRollInSpecialName")) {
            configObject.add(PREFIX + "overallRollInSpecialName", overallPercentage);
        }

        JsonElement craftedPercentages = configObject.remove(PREFIX + "craftedPercentages");
        JsonElement showRollWheel = configObject.remove(PREFIX + "showRollWheel");

        if (craftedPercentages != null || showRollWheel != null) {
            boolean percentages = craftedPercentages == null || craftedPercentages.getAsBoolean();
            boolean wheel = showRollWheel == null || showRollWheel.getAsBoolean();

            RollStyle rollStyle = percentages ? RollStyle.PERCENTAGE : wheel ? RollStyle.WHEEL : RollStyle.NONE;

            if (!configObject.has(PREFIX + "craftedOverallRollStyle")) {
                configObject.addProperty(PREFIX + "craftedOverallRollStyle", EnumUtils.toJsonFormat(rollStyle));
            }

            if (!configObject.has(PREFIX + "craftedRollStyle")) {
                configObject.addProperty(PREFIX + "craftedRollStyle", EnumUtils.toJsonFormat(rollStyle));
            }
        }

        return true;
    }
}

/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public class ActivityFunctions {
    public static class ActivityNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Activity.getTrackedName();
        }
    }

    public static class ActivityTaskFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            boolean formatted = arguments.getArgument("formatted").getBooleanValue();
            return Models.Activity.getTrackedTask().getString(formatted ? StyleType.DEFAULT : StyleType.NONE);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("formatted", Boolean.class, true)));
        }
    }

    public static class ActivityTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ActivityType type = Models.Activity.getTrackedType();
            return type != null ? type.getDisplayName() : "";
        }
    }

    public static class IsTrackingActivityFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Activity.isTracking();
        }
    }

    public static class ActivityColorFunction extends Function<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            ActivityType type = Models.Activity.getTrackedType();
            return type != null ? type.getColor() : CustomColor.NONE;
        }
    }
}

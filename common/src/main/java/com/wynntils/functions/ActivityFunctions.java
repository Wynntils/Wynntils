/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class ActivityFunctions {

    @TemplateFunction(name = "activity_name")
    public static String activityNameFunction() {
        return Models.Activity.getTrackedName();
    }

    @TemplateFunction(name = "activity_task")
    public static String activityTaskFunction(boolean formatted) {
        return Models.Activity.getTrackedTask().getString(formatted ? StyleType.DEFAULT : StyleType.NONE);
    }

    @TemplateFunction(name = "activity_type")
    public static String activityTypeFunction() {
        ActivityType type = Models.Activity.getTrackedType();
        return type != null ? type.getDisplayName() : "";
    }

    @TemplateFunction(name = "is_tracking_activity")
    public static boolean isTrackingActivityFunction() {
        return Models.Activity.isTracking();
    }

    @TemplateFunction(name = "activity_type_color")
    public static CustomColor activityColorFunction() {
        ActivityType type = Models.Activity.getTrackedType();
        return type != null ? type.getColor() : CustomColor.NONE;
    }

    @TemplateFunction(name = "activity_type_icon")
    public static StyledText activityIconFunction() {
        ActivityType type = Models.Activity.getTrackedType();
        return type != null ? StyledText.fromPart(type.getIcon()) : StyledText.EMPTY;
    }
}

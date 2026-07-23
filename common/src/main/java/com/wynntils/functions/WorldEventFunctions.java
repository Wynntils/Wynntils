/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.activities.type.WorldEvent;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class WorldEventFunctions {

    @TemplateFunction(name = "annihilation_dry_count", aliases = { "dry_annis", "dry_anni_count" })
    public static int annihilationDryCount() {
        return Models.WorldEvent.dryAnnihilations.get();
    }

    @TemplateFunction(name = "annihilation_sun_progress", aliases = { "sun_progress" })
    public static CappedValue annihilationSunProgressFunction() {
        return Models.WorldEvent.annihilationSunBar.isActive() ? Models.WorldEvent.annihilationSunBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "current_world_event")
    public static String currentWorldEventFunction() {
        WorldEvent currentWorldEvent = Models.WorldEvent.getCurrentWorldEvent();
        if (currentWorldEvent == null)
            return "";
        return currentWorldEvent.getName();
    }

    @TemplateFunction(name = "current_world_event_start_time")
    public static Time currentWorldEventStartTimeFunction() {
        WorldEvent currentWorldEvent = Models.WorldEvent.getCurrentWorldEvent();
        if (currentWorldEvent == null)
            return Time.NONE;
        return currentWorldEvent.getStartTime();
    }

    @TemplateFunction(name = "world_event_start_time")
    public static Time worldEventStartTimeFunction(String worldEventName) {
        WorldEvent worldEvent = Models.WorldEvent.getWorldEvent(worldEventName);
        if (worldEvent == null)
            return Time.NONE;
        return worldEvent.getStartTime();
    }
}

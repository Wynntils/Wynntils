/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.activities.type.WorldEvent;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import java.util.List;

public class WorldEventFunctions {
    public static class AnnihilationDryCount extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.WorldEvent.dryAnnihilations.get();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("dry_annis", "dry_anni_count");
        }
    }

    public static class AnnihilationSunProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.WorldEvent.annihilationSunBar.isActive()
                    ? Models.WorldEvent.annihilationSunBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sun_progress");
        }
    }

    public static class CurrentWorldEventFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            WorldEvent currentWorldEvent = Models.WorldEvent.getCurrentWorldEvent();

            if (currentWorldEvent == null) return "";

            return currentWorldEvent.getName();
        }
    }

    public static class CurrentWorldEventStartTimeFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            WorldEvent currentWorldEvent = Models.WorldEvent.getCurrentWorldEvent();

            if (currentWorldEvent == null) return Time.NONE;

            return currentWorldEvent.getStartTime();
        }
    }

    public static class WorldEventStartTimeFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            String worldEventName = arguments.getArgument("worldEventName").getStringValue();
            WorldEvent worldEvent = Models.WorldEvent.getWorldEvent(worldEventName);

            if (worldEvent == null) return Time.NONE;

            return worldEvent.getStartTime();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("worldEventName", String.class, null)));
        }
    }
}

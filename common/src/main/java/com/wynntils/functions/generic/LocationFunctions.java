/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.mc.type.Location;
import java.util.List;

public final class LocationFunctions {
    public static class XFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("location").getLocation().x();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("location", Location.class, null)));
        }
    }

    public static class YFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("location").getLocation().y();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("location", Location.class, null)));
        }
    }

    public static class ZFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("location").getLocation().z();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("location", Location.class, null)));
        }
    }

    public static class LocationFunction extends GenericFunction<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            return new Location(
                    arguments.getArgument("x").getIntegerValue(),
                    arguments.getArgument("y").getIntegerValue(),
                    arguments.getArgument("z").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("x", Number.class, null),
                    new Argument<>("y", Number.class, null),
                    new Argument<>("z", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("loc");
        }
    }

    public static class DistanceFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Location first = arguments.getArgument("first").getLocation();
            Location second = arguments.getArgument("second").getLocation();
            return first.toVec3().distanceTo(second.toVec3());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("first", Location.class, null), new Argument<>("second", Location.class, null)));
        }
    }
}

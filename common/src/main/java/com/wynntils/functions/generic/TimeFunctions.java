/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Time;
import java.util.List;

public final class TimeFunctions {
    public static class TimestampFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return arguments.getArgument("time").getTime().timestamp();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("time", Time.class, null)));
        }
    }

    public static class TimeFunction extends GenericFunction<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            return Time.of(arguments.getArgument("timestamp").getLongValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("timestamp", Number.class, null)));
        }
    }

    public static class AbsoluteTimeFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.formatDateTime(
                    arguments.getArgument("time").getTime().timestamp());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("time", Time.class, null)));
        }
    }

    public static class SecondsBetweenFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            long firstTimestamp = arguments.getArgument("first").getTime().timestamp();
            long secondTimestamp = arguments.getArgument("second").getTime().timestamp();

            long diffInMillis = secondTimestamp - firstTimestamp;
            return diffInMillis / 1000;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Time.class, null),
                    new FunctionArguments.Argument<>("second", Time.class, null)));
        }
    }

    public static class OffsetFunction extends GenericFunction<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            long baseTime = arguments.getArgument("time").getTime().timestamp();
            long offsetInSeconds = arguments.getArgument("offset").getLongValue() * 1000;
            return Time.of(baseTime + offsetInSeconds);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("time", Time.class, null),
                    new FunctionArguments.Argument<>("offset", Number.class, null)));
        }
    }
}

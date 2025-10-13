/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.Time;
import java.text.SimpleDateFormat;
import java.util.List;

public final class TimeFunctions {
    public static class TimestampFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return arguments.getArgument("time").getTime().timestamp();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("time", Time.class, null)));
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
                    List.of(new Argument<>("timestamp", Number.class, null)));
        }
    }

    public static class TimeStringFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("time").getTime().toString();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("time", Time.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("time_str");
        }
    }

    public static class AbsoluteTimeFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Time time = arguments.getArgument("time").getTime();
            return time.toAbsoluteString();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("time", Time.class, null)));
        }
    }

    public static class SecondsBetweenFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Time firstTime = arguments.getArgument("first").getTime();
            Time secondTime = arguments.getArgument("second").getTime();

            return firstTime.getOffset(secondTime);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Time.class, null), new Argument<>("second", Time.class, null)));
        }
    }

    public static class SecondsSinceFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Time time = arguments.getArgument("time").getTime();

            return time.getOffset(Time.now());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("time", Time.class, null)));
        }
    }

    public static class TimeOffsetFunction extends GenericFunction<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            Time baseTime = arguments.getArgument("time").getTime();
            int offsetInSeconds = arguments.getArgument("offset").getIntegerValue();
            return baseTime.offset(offsetInSeconds);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("time", Time.class, null), new Argument<>("offset", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("offset");
        }
    }

    public static class FormatTimeAdvancedFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Time timestamp = arguments.getArgument("time").getTime();
            String format = arguments.getArgument("format").getStringValue();

            try {
                return new SimpleDateFormat(format).format(timestamp.timestamp());
            } catch (IllegalArgumentException e) {
                return "Invalid Format";
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("time", Time.class, null), new Argument<>("format", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("format_date_advanced");
        }
    }
}

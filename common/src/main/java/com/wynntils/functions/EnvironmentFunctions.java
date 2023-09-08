/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.SystemUtils;
import com.wynntils.utils.type.CappedValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class EnvironmentFunctions {
    public static class CappedMemFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return new CappedValue(SystemUtils.getMemUsed(), SystemUtils.getMemMax());
        }

        @Override
        protected List<String> getAliases() {
            // FIXME: These aliases are a bit backwards, let's clean it up in the future
            return List.of("capped_memory");
        }
    }

    public static class ClockFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
            return date.format(formatter);
        }
    }

    public static class ClockmFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
            return date.format(formatter);
        }
    }

    public static class StopwatchZero extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Services.Stopwatch.isZero();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("stopwatch_is_zero");
        }
    }

    public static class StopwatchRunningFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Services.Stopwatch.isRunning();
        }
    }

    public static class StopwatchHoursFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Services.Stopwatch.getHours();
        }
    }

    public static class StopwatchMinutesFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Services.Stopwatch.getMinutes();
        }
    }

    public static class StopwatchSecondsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Services.Stopwatch.getSeconds();
        }
    }

    public static class StopwatchMillisecondsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Services.Stopwatch.getMilliseconds();
        }
    }

    public static class MemMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return SystemUtils.getMemMax();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("memorymax", "memmax");
        }
    }

    public static class MemUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return SystemUtils.getMemUsed();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("memoryused", "memused");
        }
    }

    public static class MemPctFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return (int) (((float) SystemUtils.getMemUsed() / SystemUtils.getMemMax()) * 100f);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("memorypct", "mempct");
        }
    }
}

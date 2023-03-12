/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
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

    public static class MemMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return SystemUtils.getMemMax();
        }

        @Override
        public List<String> getAliases() {
            return List.of("memorymax", "memmax");
        }
    }

    public static class MemUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return SystemUtils.getMemUsed();
        }

        @Override
        public List<String> getAliases() {
            return List.of("memoryused", "memused");
        }
    }

    public static class MemPctFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return (int) (((float) SystemUtils.getMemUsed() / SystemUtils.getMemMax()) * 100f);
        }

        @Override
        public List<String> getAliases() {
            return List.of("memorypct", "mempct");
        }
    }
}

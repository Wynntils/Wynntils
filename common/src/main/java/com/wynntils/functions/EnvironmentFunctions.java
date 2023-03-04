/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class EnvironmentFunctions {
    public static class ClockFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
            return date.format(formatter);
        }
    }

    public static class ClockmFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
            return date.format(formatter);
        }
    }

    public static class MemMaxFunction extends Function<Long> {
        @Override
        public Long getValue(String argument) {
            return Runtime.getRuntime().maxMemory() / (1024 * 1024);
        }

        @Override
        public List<String> getAliases() {
            return List.of("memorymax", "memmax");
        }
    }

    public static class MemUsedFunction extends Function<Long> {
        @Override
        public Long getValue(String argument) {
            return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        }

        @Override
        public List<String> getAliases() {
            return List.of("memoryused", "memused");
        }
    }

    public static class MemPctFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            long max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
            long used =
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

            return (int) (((float) used / max) * 100f);
        }

        @Override
        public List<String> getAliases() {
            return List.of("memorypct", "mempct");
        }
    }
}

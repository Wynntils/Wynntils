/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.services.statistics.type.StatisticEntry;
import com.wynntils.services.statistics.type.StatisticKind;
import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.resources.language.I18n;

public final class StatisticFunctions {
    public static class StatisticsTotalFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return DEFAULT_VALUE;

            return statistic.total();
        }
    }

    public static class StatisticsCountFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return DEFAULT_VALUE;

            return statistic.count();
        }
    }

    public static class StatisticsMinFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return DEFAULT_VALUE;

            return statistic.min();
        }
    }

    public static class StatisticsMaxFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return DEFAULT_VALUE;

            return statistic.max();
        }
    }

    public static class StatisticsAverageFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return DEFAULT_VALUE;

            return statistic.average();
        }
    }

    public static class StatisticsFirstModifiedTimeFunction extends StatisticFunction<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return Time.NONE;

            return Time.of(statistic.firstModified());
        }
    }

    public static class StatisticsFirstModifiedFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return 0L;

            return statistic.firstModified();
        }
    }

    public static class StatisticsLastModifiedTimeFunction extends StatisticFunction<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return Time.NONE;

            return Time.of(statistic.lastModified());
        }
    }

    public static class StatisticsLastModifiedFunction extends StatisticFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            StatisticEntry statistic = getStatisticEntry(arguments);
            if (statistic == null) return 0L;

            return statistic.lastModified();
        }
    }

    public static class StatisticsFormattedFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String statisticKey = arguments.getArgument("statisticKey").getStringValue();
            int value = arguments.getArgument("value").getIntegerValue();

            StatisticKind statisticKind = StatisticKind.from(statisticKey);
            if (statisticKind == null) return "-";

            return statisticKind.getFormattedValue(value);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("statisticKey", String.class, null), new Argument<>("value", Number.class, null)));
        }
    }

    private abstract static class StatisticFunction<T> extends Function<T> {
        protected static final long DEFAULT_VALUE = -1;

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("statisticKey", String.class, null),
                    new Argument<>("overall", Boolean.class, null)));
        }

        @Override
        public String getArgumentDescription(String argumentName) {
            if (Objects.equals(argumentName, "statisticKey")) {
                return I18n.get("function.wynntils.statistics.argument.statisticKey");
            }
            if (Objects.equals(argumentName, "overall")) {
                return I18n.get("function.wynntils.statistics.argument.overall");
            }

            return super.getArgumentDescription(argumentName);
        }

        protected static StatisticEntry getStatisticEntry(FunctionArguments arguments) {
            String statisticKey = arguments.getArgument("statisticKey").getStringValue();
            boolean overall = arguments.getArgument("overall").getBooleanValue();

            StatisticKind statisticKind = StatisticKind.from(statisticKey);
            if (statisticKind == null) return null;

            StatisticEntry statistic = overall
                    ? Services.Statistics.getOverallStatistic(statisticKind)
                    : Services.Statistics.getStatistic(statisticKind);
            return statistic;
        }
    }
}

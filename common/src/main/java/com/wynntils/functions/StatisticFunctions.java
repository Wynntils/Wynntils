/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Services;
import com.wynntils.services.statistics.type.StatisticEntry;
import com.wynntils.services.statistics.type.StatisticKind;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.resources.language.I18n;

@SuppressWarnings("unused") // Functions are accessed via reflection
public final class StatisticFunctions {


    private static Optional<StatisticEntry> getStatisticEntry(String statisticKey, boolean overall) {
        StatisticKind statisticKind = StatisticKind.from(statisticKey);
        if (statisticKind == null) return Optional.empty();

        StatisticEntry statistic = overall
                ? Services.Statistics.getOverallStatistic(statisticKind)
                : Services.Statistics.getStatistic(statisticKind);
        return Optional.of(statistic);
    }

    @TemplateFunction(name = "statistics_total")
    public static long statisticsTotalFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(StatisticEntry::total)
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_count")
    public static long statisticsCountFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(StatisticEntry::count)
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_min")
    public static long statisticsMinFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(StatisticEntry::min)
                .orElse(0L);
    }
    @TemplateFunction(name = "statistics_max")
    public static long statisticsMaxFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(StatisticEntry::max)
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_average")
    public static long statisticsAverageFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(StatisticEntry::average)
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_first_modified_time")
    public static Time statisticsFirstModifiedTimeFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(statistic -> Time.of(statistic.firstModified()))
                .orElse(Time.NONE);
    }

    @TemplateFunction(name = "statistics_first_modified")
    public static long statisticsFirstModifiedFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(statistic -> statistic.firstModified())
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_last_modified_time")
    public static Time statisticsLastModifiedTimeFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(statistic -> Time.of(statistic.lastModified()))
                .orElse(Time.NONE);
    }

    @TemplateFunction(name = "statistics_last_modified")
    public static long statisticsLastModifiedFunction(String statisticKey, boolean overall) {
        return getStatisticEntry(statisticKey, overall)
                .map(statistic -> statistic.lastModified())
                .orElse(0L);
    }

    @TemplateFunction(name = "statistics_formatted")
    public static String statisticsFormattedFunction(String statisticKey, Number value) {
        StatisticKind statisticKind = StatisticKind.from(statisticKey);
        if (statisticKind == null) return "-";

        return statisticKind.getFormattedValue(value.intValue());
    }
}

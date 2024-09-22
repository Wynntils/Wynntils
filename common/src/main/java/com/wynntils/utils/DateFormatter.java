/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class DateFormatter extends Format {
    private static final Map<ChronoUnit, String> MAPPINGS = Map.of(
            ChronoUnit.YEARS, "y",
            ChronoUnit.MONTHS, "mo",
            ChronoUnit.DAYS, "d",
            ChronoUnit.HOURS, "h",
            ChronoUnit.MINUTES, "m",
            ChronoUnit.SECONDS, "s");

    private final boolean longFormat;

    public DateFormatter(boolean longFormat) {
        this.longFormat = longFormat;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Long time) {
            if (longFormat) {
                return formatLongDate(time, toAppendTo, pos);
            } else {
                return formatShortDate(time, toAppendTo, pos);
            }
        } else {
            throw new IllegalArgumentException("Unsupported object type for date formatting");
        }
    }

    private StringBuffer formatShortDate(Long time, StringBuffer toAppendTo, FieldPosition pos) {
        Duration duration = Duration.ofMillis(time);

        Map<ChronoUnit, Long> timeUnits = new LinkedHashMap<>();

        timeUnits.put(ChronoUnit.DAYS, duration.toDays());
        duration = duration.minusDays(timeUnits.get(ChronoUnit.DAYS));

        timeUnits.put(ChronoUnit.HOURS, duration.toHours());
        duration = duration.minusHours(timeUnits.get(ChronoUnit.HOURS));

        timeUnits.put(ChronoUnit.MINUTES, duration.toMinutes());
        duration = duration.minusMinutes(timeUnits.get(ChronoUnit.MINUTES));

        timeUnits.put(ChronoUnit.SECONDS, duration.getSeconds());

        timeUnits.forEach((timeUnit, value) -> {
            if (value > 0) {
                toAppendTo.append(value).append(MAPPINGS.get(timeUnit)).append(" ");
            }
        });

        return toAppendTo;
    }

    private StringBuffer formatLongDate(Long time, StringBuffer toAppendTo, FieldPosition pos) {
        LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);

        Period period = Period.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
        Duration duration = Duration.between(startDateTime.toLocalTime(), endDateTime.toLocalTime());

        Map<ChronoUnit, Long> timeUnits = new LinkedHashMap<>();

        timeUnits.put(ChronoUnit.YEARS, (long) period.getYears());
        timeUnits.put(ChronoUnit.MONTHS, (long) period.getMonths());
        timeUnits.put(ChronoUnit.DAYS, (long) period.getDays());

        timeUnits.put(ChronoUnit.HOURS, duration.toHours());
        duration = duration.minusHours(timeUnits.get(ChronoUnit.HOURS));

        timeUnits.put(ChronoUnit.MINUTES, duration.toMinutes());
        duration = duration.minusMinutes(timeUnits.get(ChronoUnit.MINUTES));

        timeUnits.put(ChronoUnit.SECONDS, duration.getSeconds());

        timeUnits.forEach((timeUnit, value) -> {
            if (value > 0) {
                toAppendTo.append(value).append(MAPPINGS.get(timeUnit)).append(" ");
            }
        });

        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException("Parsing relative time is not supported.");
    }
}

/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.text.FieldPosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class LongDateFormatter extends DateFormatter {
    @Override
    public StringBuffer formatDate(Long time, StringBuffer toAppendTo, FieldPosition pos) {
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
}

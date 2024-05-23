/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.text.FieldPosition;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleDateFormatter extends DateFormatter {
    @Override
    public StringBuffer formatDate(Long time, StringBuffer toAppendTo, FieldPosition pos) {
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
}

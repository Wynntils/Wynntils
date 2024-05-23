/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public abstract class DateFormatter extends Format {
    protected static final Map<ChronoUnit, String> MAPPINGS = Map.of(
            ChronoUnit.YEARS, "y",
            ChronoUnit.MONTHS, "mo",
            ChronoUnit.DAYS, "d",
            ChronoUnit.HOURS, "h",
            ChronoUnit.MINUTES, "m",
            ChronoUnit.SECONDS, "s");

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Long time) {
            return formatDate(time, toAppendTo, pos);
        } else {
            throw new IllegalArgumentException("Unsupported object type for date formatting");
        }
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException("Parsing relative time is not supported.");
    }

    protected abstract StringBuffer formatDate(Long time, StringBuffer toAppendTo, FieldPosition pos);
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.collect.ImmutableMap;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class SimpleDateFormatter extends DateFormat {
    private static final Map<Integer, String> MAPPINGS = ImmutableMap.of(
            Calendar.DAY_OF_YEAR, "d",
            Calendar.HOUR_OF_DAY, "h",
            Calendar.MINUTE, "m",
            Calendar.SECOND, "s");

    public SimpleDateFormatter() {
        setCalendar(Calendar.getInstance());
        setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        calendar.setTime(date);
        StringBuffer sb = new StringBuffer();

        MAPPINGS.forEach((key, value) -> {
            int count = calendar.get(key);

            if (key == Calendar.DAY_OF_YEAR) {
                count--;
            }

            if (count > 0) {
                sb.append(count).append(value).append(" ");
            }
        });

        return sb;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        throw new UnsupportedOperationException("Parsing relative time is not supported.");
    }
}

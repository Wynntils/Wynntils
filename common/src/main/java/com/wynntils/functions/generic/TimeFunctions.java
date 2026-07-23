/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.utils.type.Time;
import java.text.SimpleDateFormat;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class TimeFunctions {

    @TemplateFunction(name = "timestamp")
    public static long timestampFunction(Time time) {
        return time.timestamp();
    }

    @TemplateFunction(name = "time")
    public static Time timeFunction(Number timestamp) {
        return Time.of(timestamp.longValue());
    }

    @TemplateFunction(name = "time_string", aliases = { "time_str" })
    public static String timeStringFunction(Time time) {
        return time.toString();
    }

    @TemplateFunction(name = "absolute_time")
    public static String absoluteTimeFunction(Time time) {
        return time.toAbsoluteString();
    }

    @TemplateFunction(name = "seconds_between")
    public static long secondsBetweenFunction(Time first, Time second) {
        return first.getOffset(second);
    }

    @TemplateFunction(name = "seconds_since")
    public static long secondsSinceFunction(Time time) {
        return time.getOffset(Time.now());
    }

    @TemplateFunction(name = "time_offset", aliases = { "offset" })
    public static Time timeOffsetFunction(Time time, Number offset) {
        Time baseTime = time;
        int offsetInSeconds = offset.intValue();
        return baseTime.offset(offsetInSeconds);
    }

    @TemplateFunction(name = "format_time_advanced", aliases = { "format_date_advanced" })
    public static String formatTimeAdvancedFunction(Time time, String format) {
        Time timestamp = time;
        try {
            return new SimpleDateFormat(format).format(timestamp.timestamp());
        } catch (IllegalArgumentException e) {
            return "Invalid Format";
        }
    }
}

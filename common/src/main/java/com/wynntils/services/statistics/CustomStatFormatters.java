/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics;

import com.wynntils.utils.StringUtils;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.stats.StatFormatter;

public final class CustomStatFormatters {
    /**
     * A time formatter that expects seconds. {@link StatFormatter.TIME} expects ticks.
     */
    public static StatFormatter TIME = (seconds) -> {
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;
        double years = days / 365.0;
        if (years >= 1) {
            return StatFormatter.DECIMAL_FORMAT.format(years) + " y";
        } else if (days >= 1) {
            return StatFormatter.DECIMAL_FORMAT.format(days) + " d";
        } else if (hours >= 1) {
            return StatFormatter.DECIMAL_FORMAT.format(hours) + " h";
        } else {
            return minutes >= 1 ? StatFormatter.DECIMAL_FORMAT.format(minutes) + " m" : seconds + " s";
        }
    };

    public static StatFormatter FORMATTED_NUMBER = (number) ->
            NumberFormat.getIntegerInstance(Locale.US).format(number) + " (" + StringUtils.formatAmount(number) + ")";
}

/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics;

import com.wynntils.services.statistics.type.StatFormatter;
import com.wynntils.utils.StringUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.Util;

public final class CustomStatFormatters {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(
            new DecimalFormat("########0.00"),
            (decimalFormat) -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    /**
     * A time formatter that expects seconds.
     */
    public static final StatFormatter TIME = (seconds) -> {
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;
        double years = days / 365.0;
        if (years >= 1) {
            return DECIMAL_FORMAT.format(years) + " y";
        } else if (days >= 1) {
            return DECIMAL_FORMAT.format(days) + " d";
        } else if (hours >= 1) {
            return DECIMAL_FORMAT.format(hours) + " h";
        } else {
            return minutes >= 1 ? DECIMAL_FORMAT.format(minutes) + " m" : seconds + " s";
        }
    };

    public static final StatFormatter FORMATTED_NUMBER = (number) ->
            NumberFormat.getIntegerInstance(Locale.US).format(number).replace(',', '.') + " ("
                    + StringUtils.integerToShortString(number) + ")";
}

/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.models.stats.StatCalculator;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.services.itemfilter.type.StatValue;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PercentageStatFilter extends StatFilter<StatValue> {
    private final float min;
    private final float max;

    public PercentageStatFilter(float min, float max) {
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean matches(StatValue value) {
        // If the item is not revealed, we can't filter percentage
        if (value.statActualValue() == null) {
            return false;
        }

        float percentage = StatCalculator.getPercentage(value.statActualValue(), value.possibleValues());

        return percentage >= min && percentage <= max;
    }

    public static class PercentageStatFilterFactory extends StatFilterFactory<PercentageStatFilter> {
        private static final Pattern SINGLE_VALUE_PATTERN = Pattern.compile("([-+]?[\\d\\.]+)%");
        private static final Pattern RANGE_PATTERN = Pattern.compile("([-+]?[\\d\\.]+)-([-+]?[\\d\\.]+)%");
        private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">=?([-+]?[\\d\\.]+)%");
        private static final Pattern LESS_THAN_PATTERN = Pattern.compile("<=?([-+]?[\\d\\.]+)%");

        @Override
        public Optional<PercentageStatFilter> create(String inputString) {
            Matcher matcher = SINGLE_VALUE_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                float value = Float.parseFloat(matcher.group(1));
                return Optional.of(new PercentageStatFilter(value, value));
            }

            matcher = RANGE_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                float min = Float.parseFloat(matcher.group(1));
                float max = Float.parseFloat(matcher.group(2));
                return Optional.of(new PercentageStatFilter(min, max));
            }

            matcher = GREATER_THAN_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                boolean equal = inputString.charAt(1) == '=';
                float value = Float.parseFloat(matcher.group(1));
                return Optional.of(new PercentageStatFilter(equal ? value : value + 1, Integer.MAX_VALUE));
            }

            matcher = LESS_THAN_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                boolean equal = inputString.charAt(1) == '=';
                float value = Float.parseFloat(matcher.group(1));
                return Optional.of(new PercentageStatFilter(Integer.MIN_VALUE, equal ? value : value - 1));
            }

            return Optional.empty();
        }
    }
}

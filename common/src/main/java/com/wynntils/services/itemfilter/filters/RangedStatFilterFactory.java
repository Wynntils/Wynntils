/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import java.util.Optional;
import java.util.regex.Pattern;

public final class RangedStatFilterFactory implements StatFilterFactory<RangedStatFilterFactory.RangedStatFilter> {
    private static final Pattern SINGLE_VALUE_PATTERN = Pattern.compile("\\d+");
    private static final Pattern RANGE_PATTERN = Pattern.compile("\\d+-\\d+");
    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">(=)?\\d+");
    private static final Pattern LESS_THAN_PATTERN = Pattern.compile("<(=)?\\d+");

    @Override
    public Optional<RangedStatFilter> create(String inputString) {
        if (SINGLE_VALUE_PATTERN.matcher(inputString).matches()) {
            int value = Integer.parseInt(inputString);
            return Optional.of(new RangedStatFilter(value, value));
        } else if (RANGE_PATTERN.matcher(inputString).matches()) {
            String[] split = inputString.split("-");
            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);
            return Optional.of(new RangedStatFilter(min, max));
        } else if (GREATER_THAN_PATTERN.matcher(inputString).matches()) {
            boolean equal = inputString.charAt(1) == '=';
            int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
            return Optional.of(new RangedStatFilter(equal ? value : value + 1, Integer.MAX_VALUE));
        } else if (LESS_THAN_PATTERN.matcher(inputString).matches()) {
            boolean equal = inputString.charAt(1) == '=';
            int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
            return Optional.of(new RangedStatFilter(Integer.MIN_VALUE, equal ? value : value - 1));
        }

        // This should not happen
        return Optional.empty();
    }

    public static final class RangedStatFilter implements StatFilter<Integer> {
        public static final RangedStatFilter ANY = new RangedStatFilter(Integer.MIN_VALUE, Integer.MAX_VALUE);

        private int min;
        private int max;

        RangedStatFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean matches(Integer value) {
            return value >= min && value <= max;
        }
    }
}

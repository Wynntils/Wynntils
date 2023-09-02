/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import java.util.regex.Pattern;

public final class RangedStatFilters {
    public static class RangedIntegerStatFilter extends StatFilter<Integer> {
        private final int min;
        private final int max;

        public RangedIntegerStatFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        protected boolean matches(Integer value) {
            return value >= min && value <= max;
        }

        public static class RangedIntegerStatFilterFactory extends RangedStatFilterFactory<RangedIntegerStatFilter> {
            @Override
            protected RangedIntegerStatFilter getRangedStatFilter(int min, int max) {
                return new RangedIntegerStatFilter(min, max);
            }
        }
    }

    public static class RangedCappedValueStatFilter extends StatFilter<CappedValue> {
        private final int min;
        private final int max;

        public RangedCappedValueStatFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        protected boolean matches(CappedValue value) {
            return value.current() >= min && value.current() <= max;
        }

        public static class RangedCappedValueStatFilterFactory
                extends RangedStatFilterFactory<RangedCappedValueStatFilter> {
            @Override
            protected RangedCappedValueStatFilter getRangedStatFilter(int min, int max) {
                return new RangedCappedValueStatFilter(min, max);
            }
        }
    }

    private abstract static class RangedStatFilterFactory<T> extends StatFilterFactory<T> {
        private static final Pattern SINGLE_VALUE_PATTERN = Pattern.compile("\\d+");
        private static final Pattern RANGE_PATTERN = Pattern.compile("\\d+-\\d+");
        private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">(=)?\\d+");
        private static final Pattern LESS_THAN_PATTERN = Pattern.compile("<(=)?\\d+");

        @Override
        public Optional<T> create(String inputString) {
            if (SINGLE_VALUE_PATTERN.matcher(inputString).matches()) {
                int value = Integer.parseInt(inputString);
                return Optional.of(getRangedStatFilter(value, value));
            } else if (RANGE_PATTERN.matcher(inputString).matches()) {
                String[] split = inputString.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                return Optional.of(getRangedStatFilter(min, max));
            } else if (GREATER_THAN_PATTERN.matcher(inputString).matches()) {
                boolean equal = inputString.charAt(1) == '=';
                int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                return Optional.of(getRangedStatFilter(equal ? value : value + 1, Integer.MAX_VALUE));
            } else if (LESS_THAN_PATTERN.matcher(inputString).matches()) {
                boolean equal = inputString.charAt(1) == '=';
                int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                return Optional.of(getRangedStatFilter(Integer.MIN_VALUE, equal ? value : value - 1));
            }

            return Optional.empty();
        }

        protected abstract T getRangedStatFilter(int min, int max);
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import java.util.regex.Matcher;
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

    public static class RangedStatValueStatFilter extends StatFilter<StatValue> {
        private final int min;
        private final int max;

        public RangedStatValueStatFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        protected boolean matches(StatValue value) {
            if (value.statActualValue() != null) {
                return value.statActualValue().value() >= min
                        && value.statActualValue().value() <= max;
            }

            return MathUtils.rangesIntersect(
                    min,
                    max,
                    value.possibleValues().range().low(),
                    value.possibleValues().range().high());
        }

        public static class RangedStatValueStatFilterFactory
                extends RangedStatFilterFactory<RangedStatValueStatFilter> {
            @Override
            protected RangedStatValueStatFilter getRangedStatFilter(int min, int max) {
                return new RangedStatValueStatFilter(min, max);
            }
        }
    }

    private abstract static class RangedStatFilterFactory<T> extends StatFilterFactory<T> {
        private static final Pattern SINGLE_VALUE_PATTERN = Pattern.compile("[-+]?\\d+");
        private static final Pattern RANGE_PATTERN = Pattern.compile("([-+]?\\d+)-([-+]?\\d+)");
        private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">(=)?[-+]?\\d+");
        private static final Pattern LESS_THAN_PATTERN = Pattern.compile("<(=)?[-+]?\\d+");

        @Override
        public Optional<T> create(String inputString) {
            Matcher matcher = SINGLE_VALUE_PATTERN.matcher(inputString);

            if (matcher.matches()) {
                int value = Integer.parseInt(inputString);
                return Optional.of(getRangedStatFilter(value, value));
            }

            matcher = RANGE_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                int min = Integer.parseInt(matcher.group(1));
                int max = Integer.parseInt(matcher.group(2));
                return Optional.of(getRangedStatFilter(min, max));
            }

            matcher = GREATER_THAN_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                boolean equal = inputString.charAt(1) == '=';
                int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                return Optional.of(getRangedStatFilter(equal ? value : value + 1, Integer.MAX_VALUE));
            }

            matcher = LESS_THAN_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                boolean equal = inputString.charAt(1) == '=';
                int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                return Optional.of(getRangedStatFilter(Integer.MIN_VALUE, equal ? value : value - 1));
            }

            return Optional.empty();
        }

        protected abstract T getRangedStatFilter(int min, int max);
    }
}

/*
 * Copyright © Wynntils 2023-2025.
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
    public static class RangedIntegerStatFilter extends AbstractRangedStatFilter<Integer> {
        protected RangedIntegerStatFilter(int min, int max, boolean equalsInString) {
            super(min, max, equalsInString);
        }

        @Override
        public boolean matches(Integer value) {
            return value >= min && value <= max;
        }

        public static class RangedIntegerStatFilterFactory extends RangedStatFilterFactory<RangedIntegerStatFilter> {
            @Override
            protected RangedIntegerStatFilter getRangedStatFilter(int min, int max, boolean equalsInString) {
                return new RangedIntegerStatFilter(min, max, equalsInString);
            }
        }
    }

    public static class RangedCappedValueStatFilter extends AbstractRangedStatFilter<CappedValue> {
        protected RangedCappedValueStatFilter(int min, int max, boolean equalsInString) {
            super(min, max, equalsInString);
        }

        @Override
        public boolean matches(CappedValue value) {
            return value.current() >= min && value.current() <= max;
        }

        public static class RangedCappedValueStatFilterFactory
                extends RangedStatFilterFactory<RangedCappedValueStatFilter> {
            @Override
            protected RangedCappedValueStatFilter getRangedStatFilter(int min, int max, boolean equalsInString) {
                return new RangedCappedValueStatFilter(min, max, equalsInString);
            }
        }
    }

    public static class RangedStatValueStatFilter extends AbstractRangedStatFilter<StatValue> {
        protected RangedStatValueStatFilter(int min, int max, boolean equalsInString) {
            super(min, max, equalsInString);
        }

        @Override
        public boolean matches(StatValue value) {
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
            protected RangedStatValueStatFilter getRangedStatFilter(int min, int max, boolean equalsInString) {
                return new RangedStatValueStatFilter(min, max, equalsInString);
            }
        }
    }

    public abstract static class AbstractRangedStatFilter<T> extends StatFilter<T> {
        protected final int min;
        protected final int max;

        protected final boolean equalsInString;

        protected AbstractRangedStatFilter(int min, int max, boolean equalsInString) {
            this.min = min;
            this.max = max;
            this.equalsInString = equalsInString;
        }

        @Override
        public String asString() {
            if (min == max) {
                return Integer.toString(min);
            }

            if (min == Integer.MIN_VALUE) {
                return equalsInString ? "<=" + max : "<" + (max + 1);
            }

            if (max == Integer.MAX_VALUE) {
                return equalsInString ? ">=" + min : ">" + (min - 1);
            }

            return min + "-" + max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public boolean isEqualsInString() {
            return equalsInString;
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

            try {
                if (matcher.matches()) {
                    int value = Integer.parseInt(inputString);
                    return Optional.of(getRangedStatFilter(value, value, true));
                }

                matcher = RANGE_PATTERN.matcher(inputString);
                if (matcher.matches()) {
                    int min = Integer.parseInt(matcher.group(1));
                    int max = Integer.parseInt(matcher.group(2));
                    return Optional.of(getRangedStatFilter(min, max, true));
                }

                matcher = GREATER_THAN_PATTERN.matcher(inputString);
                if (matcher.matches()) {
                    boolean equal = inputString.charAt(1) == '=';
                    int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                    return Optional.of(getRangedStatFilter(equal ? value : value + 1, Integer.MAX_VALUE, equal));
                }

                matcher = LESS_THAN_PATTERN.matcher(inputString);
                if (matcher.matches()) {
                    boolean equal = inputString.charAt(1) == '=';
                    int value = Integer.parseInt(inputString.substring(equal ? 2 : 1));
                    return Optional.of(getRangedStatFilter(Integer.MIN_VALUE, equal ? value : value - 1, equal));
                }
            } catch (NumberFormatException e) {
                return Optional.empty();
            }

            return Optional.empty();
        }

        protected abstract T getRangedStatFilter(int min, int max, boolean equalsInString);
    }
}

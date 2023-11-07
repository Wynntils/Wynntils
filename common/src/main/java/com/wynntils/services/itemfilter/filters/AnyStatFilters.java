/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class AnyStatFilters {
    public static final class AnyStringStatFilter extends StatFilter<String> {
        @Override
        public boolean matches(String value) {
            return true;
        }

        public static final class AnyStringStatFilterFactory extends AbstractAnyStatFilterFactory<AnyStringStatFilter> {
            @Override
            protected AnyStringStatFilter getAnyStatFilter() {
                return new AnyStringStatFilter();
            }
        }
    }

    public static final class AnyIntegerStatFilter extends StatFilter<Integer> {
        @Override
        public boolean matches(Integer value) {
            return true;
        }

        public static final class AnyIntegerStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyIntegerStatFilter> {
            @Override
            protected AnyIntegerStatFilter getAnyStatFilter() {
                return new AnyIntegerStatFilter();
            }
        }
    }

    public static final class AnyCappedValueStatFilter extends StatFilter<CappedValue> {
        @Override
        public boolean matches(CappedValue value) {
            return true;
        }

        public static final class AnyCappedValueStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyCappedValueStatFilter> {
            @Override
            protected AnyCappedValueStatFilter getAnyStatFilter() {
                return new AnyCappedValueStatFilter();
            }
        }
    }

    public static final class AnyStatValueStatFilter extends StatFilter<StatValue> {
        @Override
        public boolean matches(StatValue value) {
            return true;
        }

        public static final class AnyStatValueStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyStatValueStatFilter> {
            @Override
            protected AnyStatValueStatFilter getAnyStatFilter() {
                return new AnyStatValueStatFilter();
            }
        }
    }

    private abstract static class AbstractAnyStatFilterFactory<T> extends StatFilterFactory<T> {
        private static final String ANY_FILTER_INPUT = "*";

        @Override
        public final Optional<T> create(String inputString) {
            if (inputString.equals(ANY_FILTER_INPUT)) {
                return Optional.of(getAnyStatFilter());
            }

            return Optional.empty();
        }

        protected abstract T getAnyStatFilter();
    }
}

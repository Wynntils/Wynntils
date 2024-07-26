/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class AnyStatFilters {
    private static final String ANY_FILTER_INPUT = "*";

    public static final class AnyStringStatFilter extends AbstractAnyStatFilter<String> {
        public static final class AnyStringStatFilterFactory extends AbstractAnyStatFilterFactory<AnyStringStatFilter> {
            @Override
            protected AnyStringStatFilter getAnyStatFilter() {
                return new AnyStringStatFilter();
            }
        }
    }

    public static final class AnyIntegerStatFilter extends AbstractAnyStatFilter<Integer> {
        public static final class AnyIntegerStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyIntegerStatFilter> {
            @Override
            protected AnyIntegerStatFilter getAnyStatFilter() {
                return new AnyIntegerStatFilter();
            }
        }
    }

    public static final class AnyCappedValueStatFilter extends AbstractAnyStatFilter<CappedValue> {
        public static final class AnyCappedValueStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyCappedValueStatFilter> {
            @Override
            protected AnyCappedValueStatFilter getAnyStatFilter() {
                return new AnyCappedValueStatFilter();
            }
        }
    }

    public static final class AnyStatValueStatFilter extends AbstractAnyStatFilter<StatValue> {
        public static final class AnyStatValueStatFilterFactory
                extends AbstractAnyStatFilterFactory<AnyStatValueStatFilter> {
            @Override
            protected AnyStatValueStatFilter getAnyStatFilter() {
                return new AnyStatValueStatFilter();
            }
        }
    }

    public abstract static class AbstractAnyStatFilter<T> extends StatFilter<T> {
        @Override
        public final boolean matches(T value) {
            return true;
        }

        @Override
        public final String asString() {
            return ANY_FILTER_INPUT;
        }
    }

    private abstract static class AbstractAnyStatFilterFactory<T> extends StatFilterFactory<T> {
        @Override
        public final Optional<T> create(String inputString) {
            if (inputString.equals(ANY_FILTER_INPUT)) {
                return Optional.of(getAnyStatFilter());
            }

            return Optional.empty();
        }

        public final T create() {
            return getAnyStatFilter();
        }

        protected abstract T getAnyStatFilter();
    }
}

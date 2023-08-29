/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import java.util.Optional;

public class AnyIntegerStatFilter extends StatFilter<Integer> {
    @Override
    protected boolean matches(Integer value) {
        return true;
    }

    public static class AnyIntegerStatFilterFactory extends StatFilterFactory<AnyIntegerStatFilter> {
        private static final String FILTER_INPUT = "*";

        @Override
        public Optional<AnyIntegerStatFilter> create(String inputString) {
            if (inputString.equals(FILTER_INPUT)) {
                return Optional.of(new AnyIntegerStatFilter());
            }

            return Optional.empty();
        }
    }
}

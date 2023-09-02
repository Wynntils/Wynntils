/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import java.util.Optional;

public class AnyStringStatFilter extends StatFilter<String> {
    @Override
    protected boolean matches(String value) {
        return true;
    }

    public static class AnyStringStatFilterFactory extends StatFilterFactory<AnyStringStatFilter> {
        private static final String FILTER_INPUT = "*";

        @Override
        public Optional<AnyStringStatFilter> create(String inputString) {
            if (inputString.equals(FILTER_INPUT)) {
                return Optional.of(new AnyStringStatFilter());
            }

            return Optional.empty();
        }
    }
}

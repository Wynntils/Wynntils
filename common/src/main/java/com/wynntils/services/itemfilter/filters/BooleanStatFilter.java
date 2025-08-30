/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import java.util.Optional;

public class BooleanStatFilter extends StatFilter<Boolean> {
    private final boolean value;

    private BooleanStatFilter(boolean value) {
        this.value = value;
    }

    @Override
    public boolean matches(Boolean value) {
        return this.value == value;
    }

    @Override
    public String asString() {
        return Boolean.toString(value);
    }

    public static class BooleanStatFilterFactory extends StatFilterFactory<BooleanStatFilter> {
        @Override
        public Optional<BooleanStatFilter> create(String inputString) {
            if (inputString.equalsIgnoreCase("true")) {
                return Optional.of(new BooleanStatFilter(true));
            } else if (inputString.equalsIgnoreCase("false")) {
                return Optional.of(new BooleanStatFilter(false));
            } else {
                return Optional.empty();
            }
        }

        public BooleanStatFilter fromBoolean(boolean value) {
            return new BooleanStatFilter(value);
        }
    }
}

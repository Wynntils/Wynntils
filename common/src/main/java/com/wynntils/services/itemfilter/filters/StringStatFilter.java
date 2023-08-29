/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.utils.StringUtils;
import java.util.Optional;

public final class StringStatFilter extends StatFilter<String> {
    private String searchLiteral;
    private boolean strict;

    StringStatFilter(String searchLiteral, boolean strict) {
        this.searchLiteral = searchLiteral;
        this.strict = strict;
    }

    @Override
    public boolean matches(String value) {
        return strict ? value.equalsIgnoreCase(searchLiteral) : StringUtils.containsIgnoreCase(value, searchLiteral);
    }

    public static class StringStatFilterFactory extends StatFilterFactory<StringStatFilter> {
        private static final String STRICT_FILTER_CHAR = "\"";

        @Override
        public Optional<StringStatFilter> create(String inputString) {
            if (inputString.startsWith(STRICT_FILTER_CHAR) && inputString.endsWith(STRICT_FILTER_CHAR)) {
                return Optional.of(new StringStatFilter(inputString.substring(1, inputString.length() - 1), true));
            }

            return Optional.of(new StringStatFilter(inputString, false));
        }
    }
}

/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.utils.StringUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringStatFilter extends StatFilter<String> {
    private final String searchLiteral;
    private final boolean strict;

    private StringStatFilter(String searchLiteral, boolean strict) {
        this.searchLiteral = searchLiteral;
        this.strict = strict;
    }

    @Override
    public boolean matches(String value) {
        return strict ? value.equalsIgnoreCase(searchLiteral) : StringUtils.containsIgnoreCase(value, searchLiteral);
    }

    @Override
    public String asString() {
        return strict ? "\"" + searchLiteral + "\"" : searchLiteral;
    }

    public boolean isStrict() {
        return strict;
    }

    public String getSearchLiteral() {
        return searchLiteral;
    }

    public static class StringStatFilterFactory extends StatFilterFactory<StringStatFilter> {
        private static final Pattern STRICT_FILTER_PATTERN = Pattern.compile("\"(.+)\"");

        @Override
        public Optional<StringStatFilter> create(String inputString) {
            Matcher matcher = STRICT_FILTER_PATTERN.matcher(inputString);
            if (matcher.matches()) {
                return Optional.of(new StringStatFilter(matcher.group(1), true));
            }

            return Optional.of(new StringStatFilter(inputString, false));
        }
    }
}

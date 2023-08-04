/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import static java.lang.Integer.parseInt;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.LeveledItemProperty;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelItemFilter extends ItemFilter {
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("^(\\d+)(?:-(\\d+))?$");

    private int minLevel;
    private int maxLevel;

    private boolean prepared = false;

    public LevelItemFilter(String filterValue) {
        super(filterValue);
    }

    @Override
    public void prepare() throws InvalidSyntaxException {
        Matcher matcher = LEVEL_RANGE_PATTERN.matcher(filterValue);
        if (!matcher.find()) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.invalidRange", filterValue);
        }

        try {
            minLevel = parseInt(matcher.group(1));
            maxLevel = matcher.group(2) == null ? minLevel : parseInt(matcher.group(2));
        } catch (NumberFormatException ignore) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.invalidRange", filterValue);
        }

        if (minLevel > maxLevel) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.maxLessThanMin", filterValue);
        }

        prepared = true;
    }

    @Override
    public boolean matches(WynnItem wynnItem) throws IllegalStateException {
        if (!prepared) {
            throw new IllegalStateException("LevelItemFilter must be prepared before use");
        }

        return wynnItem instanceof LeveledItemProperty leveledItem
                && leveledItem.getLevel() >= minLevel
                && maxLevel >= leveledItem.getLevel();
    }
}

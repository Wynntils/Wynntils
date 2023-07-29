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

public class LevelSearchFilter extends ItemFilter {
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("^(\\d+)(-(\\d+))?$");

    private int minLevel, maxLevel;

    private boolean prepared = false;

    public LevelSearchFilter(String searchString) {
        super(searchString);
    }

    @Override
    public void prepare() throws InvalidSyntaxException {
        Matcher matcher = LEVEL_RANGE_PATTERN.matcher(searchString);
        if (!matcher.find()) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.invalidRange", searchString);
        }

        try {
            minLevel = parseInt(matcher.group(1));
            maxLevel = matcher.group(3) == null ? minLevel : parseInt(matcher.group(3));
        } catch (NumberFormatException ignore) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.invalidRange", searchString);
        }

        if (minLevel > maxLevel) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.level.maxLessThanMin", searchString);
        }

        prepared = true;
    }

    @Override
    public boolean matches(WynnItem wynnItem) throws IllegalStateException {
        if (!prepared) {
            throw new IllegalStateException("LevelSearchFilter must be prepared before use");
        }

        return wynnItem instanceof LeveledItemProperty leveledItem
                && leveledItem.getLevel() >= minLevel
                && maxLevel >= leveledItem.getLevel();
    }
}

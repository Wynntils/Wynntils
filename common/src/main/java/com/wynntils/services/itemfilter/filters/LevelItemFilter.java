/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import static java.lang.Integer.parseInt;

import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.services.itemfilter.type.ItemFilter;
import com.wynntils.services.itemfilter.type.ItemFilterInstance;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelItemFilter extends ItemFilter {
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("^(\\d+)(?:-(\\d+))?$");

    public LevelItemFilter() {
        super(List.of("lvl"));
    }

    @Override
    public ErrorOr<ItemFilterInstance> createInstance(String inputString) {
        int minLevel;
        int maxLevel;
        Matcher matcher = LEVEL_RANGE_PATTERN.matcher(inputString);
        if (!matcher.find()) {
            return ErrorOr.error(getTranslation("invalidRange", inputString));
        }

        try {
            minLevel = parseInt(matcher.group(1));
            maxLevel = matcher.group(2) == null ? minLevel : parseInt(matcher.group(2));
        } catch (NumberFormatException ignore) {
            return ErrorOr.error(getTranslation("invalidRange", inputString));
        }

        if (minLevel > maxLevel) {
            return ErrorOr.error(getTranslation("maxLessThanMin", inputString));
        }

        return ErrorOr.of(wynnItem -> wynnItem instanceof LeveledItemProperty leveledItem
                && leveledItem.getLevel() >= minLevel
                && maxLevel >= leveledItem.getLevel());
    }
}

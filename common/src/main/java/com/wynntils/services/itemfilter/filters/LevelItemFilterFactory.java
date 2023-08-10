/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import static java.lang.Integer.parseInt;

import com.wynntils.services.itemfilter.type.ItemFilterFactory;
import com.wynntils.utils.type.ErrorOr;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.resources.language.I18n;

public class LevelItemFilterFactory implements ItemFilterFactory {
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("^(\\d+)(?:-(\\d+))?$");

    @Override
    public ErrorOr<LevelItemFilter> create(String inputString) {
        int minLevel;
        int maxLevel;
        Matcher matcher = LEVEL_RANGE_PATTERN.matcher(inputString);
        if (!matcher.find()) {
            return ErrorOr.error(I18n.get(getI18nKey() + ".invalidRange", inputString));
        }

        try {
            minLevel = parseInt(matcher.group(1));
            maxLevel = matcher.group(2) == null ? minLevel : parseInt(matcher.group(2));
        } catch (NumberFormatException ignore) {
            return ErrorOr.error(I18n.get(getI18nKey() + ".invalidRange", inputString));
        }

        if (minLevel > maxLevel) {
            return ErrorOr.error(I18n.get(getI18nKey() + ".maxLessThanMin", inputString));
        }

        return ErrorOr.of(new LevelItemFilter(minLevel, maxLevel));
    }

    @Override
    public String getKeyword() {
        return "lvl";
    }

    @Override
    public String getI18nKey() {
        return "feature.wynntils.itemFilter.level";
    }
}

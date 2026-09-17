/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionGamemodesSegment;
import com.wynntils.models.character.type.CharacterGamemode;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterSelectionGamemodesSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern GAMEMODES_PATTERN =
            Pattern.compile("[\uE020-\uE024]+(?:\uDB00\uDC02[\uE020-\uE024]+)*\uDB00\uDC27");

    private static final Pattern ICON_PATTERN = Pattern.compile("[\uE020-\uE024]");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();

        Matcher segmentMatcher = GAMEMODES_PATTERN.matcher(actionBarString);
        if (!segmentMatcher.find()) return null;

        String segment = segmentMatcher.group();

        Set<CharacterGamemode> gamemodes = EnumSet.noneOf(CharacterGamemode.class);
        ICON_PATTERN
                .matcher(segment)
                .results()
                .map(MatchResult::group)
                .map(CharacterGamemode::fromSelectedIcon)
                .filter(Objects::nonNull)
                .forEach(gamemodes::add);

        if (gamemodes.isEmpty()) return null;

        return new CharacterSelectionGamemodesSegment(segment, segmentMatcher.start(), segmentMatcher.end(), gamemodes);
    }
}

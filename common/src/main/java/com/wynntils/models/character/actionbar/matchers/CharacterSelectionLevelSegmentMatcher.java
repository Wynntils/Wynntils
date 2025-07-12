/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionLevelSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterSelectionLevelSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern CHARACTER_LEVEL_PATTERN =
            Pattern.compile("\uDAFF\uDF8C\u0001([\uE000-\uE004])\uDB00\uDC0A");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = CHARACTER_LEVEL_PATTERN.matcher(actionBar);
        if (!matcher.find()) return null;

        int level = 106;

        return new CharacterSelectionLevelSegment(actionBar, level);
    }
}

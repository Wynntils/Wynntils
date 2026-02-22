/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterSelectionSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern CHARACTER_CREATION_PATTERN =
            Pattern.compile("\uE000 Left-Click to play                     \uE001 Right-Click to switch");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = CHARACTER_CREATION_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        return new CharacterSelectionSegment(matcher.group(), matcher.start(), matcher.end());
    }
}

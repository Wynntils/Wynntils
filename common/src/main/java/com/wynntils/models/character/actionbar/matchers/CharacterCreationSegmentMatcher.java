/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterCreationSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterCreationSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern CHARACTER_CREATION_PATTERN = Pattern.compile(
            "\uE000 Left-Click to select          \uE002 Scroll up/down to browse(?<returnText>          \uE001 Right-Click to return)?");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = CHARACTER_CREATION_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        return new CharacterCreationSegment(
                matcher.group(), matcher.start(), matcher.end(), matcher.group("returnText") == null);
    }
}

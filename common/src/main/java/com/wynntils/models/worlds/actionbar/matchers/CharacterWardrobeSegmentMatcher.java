/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.worlds.actionbar.segments.CharacterWardrobeSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterWardrobeSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern CHARACTER_WARDROBE_PATTERN = Pattern.compile(
            "C\uDAFF\uDFFEh\uDAFF\uDFFEa\uDAFF\uDFFEr\uDAFF\uDFFEa\uDAFF\uDFFEc\uDAFF\uDFFEt\uDAFF\uDFFEe\uDAFF\uDFFEr W\uDAFF\uDFFEa\uDAFF\uDFFEr\uDAFF\uDFFEd\uDAFF\uDFFEr\uDAFF\uDFFEo\uDAFF\uDFFEb\uDAFF\uDFFEe");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = CHARACTER_WARDROBE_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        return new CharacterWardrobeSegment(matcher.group(), matcher.start(), matcher.end());
    }
}

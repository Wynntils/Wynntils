/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterWardrobeSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterWardrobeSegmentMacher implements ActionBarSegmentMatcher {
    private static final Pattern CHARACTER_WARDROBE_PATTERN = Pattern.compile(
            "\u0001C\uDAFF\uDFFEh\uDAFF\uDFFEa\uDAFF\uDFFEr\uDAFF\uDFFEa\uDAFF\uDFFEc\uDAFF\uDFFEt\uDAFF\uDFFEe\uDAFF\uDFFEr W\uDAFF\uDFFEa\uDAFF\uDFFEr\uDAFF\uDFFEd\uDAFF\uDFFEr\uDAFF\uDFFEo\uDAFF\uDFFEb\uDAFF\uDFFEe\uDAFF\uDFD0");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = CHARACTER_WARDROBE_PATTERN.matcher(actionBar);
        if (!matcher.find()) return null;

        return new CharacterWardrobeSegment(actionBar);
    }
}

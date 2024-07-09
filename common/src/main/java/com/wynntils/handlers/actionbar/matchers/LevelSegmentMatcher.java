/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.LevelSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelSegmentMatcher implements ActionBarSegmentMatcher {
    // The start and end characters' high surrogate for the level segment
    protected static final char SPACER_HIGH_SURROGATE = '\uDAFF';

    // A separator between the level characters
    private static final String SEPARATOR = "\uDAFF\uDFFE";

    // Possible character range for the level segment, collected from the resource pack/font
    private static final String LEVEL_CHARS = "\uE000-\uE009";

    // The expected pattern for the level segment
    // Allow 6 characters for the level, because the level is 3 digits + 2-3 separator characters
    // There is also a space character at the start and end of the segment
    private static final Pattern LEVEL_PATTERN = Pattern.compile(".[" + LEVEL_CHARS + SEPARATOR + "]{1,6}.");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = LEVEL_PATTERN.matcher(actionBar);
        if (!matcher.find()) return null;

        String segmentText = matcher.group();

        // First unicode character's high surrogate
        char startChar = segmentText.charAt(0);

        // Last unicode character's high surrogate
        char endChar = segmentText.charAt(segmentText.length() - 2);

        // Check if the segment text is surrounded by the correct separators
        boolean validStart = startChar == SPACER_HIGH_SURROGATE;
        boolean validEnd = endChar == SPACER_HIGH_SURROGATE;

        if (!validStart || !validEnd) return null;

        return new LevelSegment(matcher.group());
    }
}

/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A common base class for matching health and mana text segments in the action bar.
 */
public abstract class AbstractTextSegmentMatcher implements ActionBarSegmentMatcher {
    // High surrogate characters for the positive and negative space characters
    // These are used to tell the difference between health and mana text
    protected static final char POSITIVE_SPACE_HIGH_SURROGATE = '\uDB00';
    protected static final char NEGATIVE_SPACE_HIGH_SURROGATE = '\uDAFF';

    // The separator between the display characters, usually before and after the "/" in the mana/health text
    private static final String SEPARATOR = "\uDB00\uDC02";

    // All possible display character ranges, extracted from the resource pack/font
    private static final String DISPLAY_CHARACTERS = "\uE010-\uE01F";

    /**
     * Get the separators of the segment text in the action bar.
     * Both the start and end characters appear only once in the segment.
     * Start and end characters are not always in pairs
     * (a start character does not always have the same, corresponding end character).
     * @return The separators of the segment text
     */
    protected abstract SegmentSeparators segmentSeparators();

    protected abstract ActionBarSegment createSegment(String segmentText);

    // There is a spacing character before and after actual display characters
    private final Pattern segmentPattern = Pattern.compile(".[" + DISPLAY_CHARACTERS + SEPARATOR + "]+.");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = segmentPattern.matcher(actionBar);

        if (!matcher.find()) return null;

        // Loop, because we may find other text segments that we are not looking for
        // (e.g. health text when looking for mana text)
        do {
            String segmentText = matcher.group();

            // Verify that the segment text is surrounded by the correct separators for this segment
            SegmentSeparators separators = segmentSeparators();

            // First unicode character's high surrogate
            char startChar = segmentText.charAt(0);

            // Last unicode character's high surrogate
            char endChar = segmentText.charAt(segmentText.length() - 2);

            // Check if the segment text is surrounded by the correct separators
            boolean validStart = startChar == separators.segmentStart;
            boolean validEnd = endChar == separators.segmentEnd;

            if (!validStart || !validEnd) continue;

            return createSegment(segmentText);
        } while (matcher.find());

        return null;
    }

    protected record SegmentSeparators(char segmentStart, char segmentEnd) {}
}

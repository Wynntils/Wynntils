/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A common base class for all the meter segments in the action bar (center hexagon with level, sprint and breath bars).
 */
public abstract class AbstractMeterSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a meter segment, a spacer
    private static final String SEGMENT_START = "\uDAFF\uDFF4";
    // The end of a meter segment, a spacer
    private static final String SEGMENT_END = "\uDAFF\uDFF3";

    private final Pattern segmentMatcher = Pattern.compile(SEGMENT_START
            + (isMultipleSegments()
                    ? "(?<value>[" + String.join("", getCharacterRange()) + "])+"
                    : "(?<value>[" + String.join("", getCharacterRange()) + "])")
            + SEGMENT_END);

    protected abstract List<String> getCharacterRange();

    /**
     * Override this method if the segment matcher should match multiple characters in the range for this segment.
     * @return true if the matcher can match multiple characters, false if it should only match a single character.
     */
    protected boolean isMultipleSegments() {
        return false;
    }

    protected abstract ActionBarSegment createSegment(
            String segmentText, int startIndex, int endIndex, String segmentValue);

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = this.segmentMatcher.matcher(actionBarString);
        if (!matcher.find()) return null;
        return createSegment(matcher.group(), matcher.start(), matcher.end(), matcher.group("value"));
    }
}

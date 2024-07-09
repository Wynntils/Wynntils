/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMeterSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a meter segment, a spacer
    private static final String SEGMENT_START = "\uDAFF\uDFF4";
    // The end of a meter segment, a spacer
    private static final String SEGMENT_END = "\uDAFF\uDFF3";

    private final Pattern meterRegex =
            Pattern.compile(SEGMENT_START + "[" + String.join("", getCharacterRange()) + "]" + SEGMENT_END);

    protected abstract List<String> getCharacterRange();

    protected abstract ActionBarSegment createSegment(String segmentText);

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = meterRegex.matcher(actionBar);
        if (!matcher.find()) return null;
        return createSegment(matcher.group());
    }
}

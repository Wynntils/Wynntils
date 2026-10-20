/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.timetrials.actionbar.segments.TimeTrialCheckpointTimeSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTrialCheckpointTimeSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String SEGMENT_START = "\uDB00\uDC29\u0001";

    private static final String TIME_PATTERN = "(?:(?<minute>\\d+):)?(?<seconds>\\d+).(?<milliseconds>\\d+)";

    private static final char SPACER_END_HIGH_SURROGATE = '\uDAFF';

    private static final Pattern SEGMENT_PATTERN = Pattern.compile(SEGMENT_START + TIME_PATTERN + ".");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = SEGMENT_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        String segmentText = matcher.group();

        // Last unicode character's high surrogate
        char endChar = segmentText.charAt(segmentText.length() - 2);

        // Check if the segment text is surrounded by the correct separators
        boolean validEnd = endChar == SPACER_END_HIGH_SURROGATE;

        if (!validEnd) return null;

        long checkpointTime =
                parseCheckpointTime(matcher.group("minute"), matcher.group("seconds"), matcher.group("milliseconds"));

        return new TimeTrialCheckpointTimeSegment(matcher.group(), matcher.start(), matcher.end(), checkpointTime);
    }

    private long parseCheckpointTime(String minutesString, String secondsString, String millisecondsString) {
        try {
            long minutes = minutesString != null ? Long.parseLong(minutesString) : 0;
            long seconds = Long.parseLong(secondsString);
            long milliseconds = Long.parseLong(millisecondsString) * 10;

            return minutes * 60_000L + seconds * 1_000L + milliseconds;
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }
}

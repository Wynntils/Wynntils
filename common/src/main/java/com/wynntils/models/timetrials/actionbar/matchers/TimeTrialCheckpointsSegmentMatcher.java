/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.timetrials.actionbar.segments.TimeTrialCheckpointsSegment;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTrialCheckpointsSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String SEGMENT_START = "\uDB00\uDC1C\u0001";

    private static final char SPACER_END_HIGH_SURROGATE = '\uDAFF';

    private static final String CHECKPOINT_ICON = "\uE001";

    private static final String CHECKPOINTS_PATTERN = "(?<currentCheckpoint>\\d+)\\/(?<totalCheckpoints>\\d+)";

    private static final Pattern SEGMENT_PATTERN =
            Pattern.compile(SEGMENT_START + CHECKPOINT_ICON + " " + CHECKPOINTS_PATTERN + ".");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = SEGMENT_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        String segmentText = matcher.group();

        // Last unicode character's high surrogate
        char endChar = segmentText.charAt(segmentText.length() - 2);

        // Check if the segment text ends with by the correct separator
        boolean validEnd = endChar == SPACER_END_HIGH_SURROGATE;

        if (!validEnd) return null;

        CappedValue checkpoints = new CappedValue(
                Integer.parseInt(matcher.group("currentCheckpoint")),
                Integer.parseInt(matcher.group("totalCheckpoints")));

        return new TimeTrialCheckpointsSegment(matcher.group(), matcher.start(), matcher.end(), checkpoints);
    }
}

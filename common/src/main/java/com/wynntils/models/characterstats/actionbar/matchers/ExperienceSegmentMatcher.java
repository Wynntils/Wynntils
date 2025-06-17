/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ExperienceSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a experience segment, a spacer
    private static final String SEGMENT_START = "\uDAFF\uDFA7";

    // The end of a experience segment, a spacer
    private static final String SEGMENT_END = "\uDAFF\uDFA5";

    private final Pattern experienceBarPattern = Pattern.compile(
            SEGMENT_START + "([" + getExperienceCharStart() + "-" + getExperienceCharEnd() + "])" + SEGMENT_END);

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = experienceBarPattern.matcher(actionBar);
        if (!matcher.find()) {
            return null;
        }

        String levelSegmentText = matcher.group(1);

        // Calculate the progress based on experience bar character start and end
        int startChar = getExperienceCharStart().codePointAt(0);
        int endChar = getExperienceCharEnd().codePointAt(0);
        int progress = levelSegmentText.codePointAt(0) - startChar;

        CappedValue cappedProgress = new CappedValue(progress, endChar - startChar + 1);
        return createExperienceSegment(matcher.group(), cappedProgress);
    }

    protected abstract String getExperienceCharStart();

    protected abstract String getExperienceCharEnd();

    protected abstract ActionBarSegment createExperienceSegment(String levelSegmentText, CappedValue progress);
}

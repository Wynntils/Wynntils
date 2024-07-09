/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.SpellSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellSegmentMatcher implements ActionBarSegmentMatcher {
    // The start and end of a spell segment, a spacer
    private static final String SEGMENT_SEPARATOR = "\uDAFF\uDFE0";

    // The character for left click
    private static final String LEFT_CLICK = "[\uE100\uE103]";

    // The character for right click
    private static final String RIGHT_CLICK = "[\uE101|\uE104]";

    // The character for no click (yet)
    private static final String NO_CLICK = "[\uE102\uE105]";

    // The separator between the spell clicks (space + arrow)
    private static final String SEPARATOR = "\\s\uE106\\s";

    private static final Pattern SPELL_REGEX = Pattern.compile(SEGMENT_SEPARATOR
            + "(" + LEFT_CLICK + "|" + RIGHT_CLICK + "|" + NO_CLICK + ")" + SEPARATOR
            + "(" + LEFT_CLICK + "|" + RIGHT_CLICK + "|" + NO_CLICK + ")" + SEPARATOR
            + "(" + LEFT_CLICK + "|" + RIGHT_CLICK + "|" + NO_CLICK + ")" + SEGMENT_SEPARATOR);

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = SPELL_REGEX.matcher(actionBar);
        if (!matcher.find()) return null;
        return new SpellSegment(matcher.group());
    }
}

/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.HotbarSegment;

public class HotbarSegmentMatcher implements ActionBarSegmentMatcher {
    // This is the expected string for the hotbar, which should be "static" and not change
    private static final String HOTBAR_STRING = "\uDAFF\uDF98\uE00A\uDAFF\uDFFF\uDAFF\uDF98";

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        int index = actionBarString.indexOf(HOTBAR_STRING);
        if (index == -1) return null;
        return new HotbarSegment(HOTBAR_STRING, index, index + HOTBAR_STRING.length());
    }
}

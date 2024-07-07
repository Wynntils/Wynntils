/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.HotbarSegment;
import com.wynntils.utils.type.StringReader;

public class HotbarSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String EXPECTED_STRING = "\uDAFF\uDF98\uE00A\uDAFF\uDFFF\uDAFF\uDF98";

    @Override
    public ActionBarSegment read(StringReader reader) {
        // Read the hotbar segment from the reader, check if it matches the expected string
        String segmentText = reader.read(EXPECTED_STRING.length());
        if (!segmentText.equals(EXPECTED_STRING)) {
            return null;
        } else {
            return new HotbarSegment(segmentText);
        }
    }
}

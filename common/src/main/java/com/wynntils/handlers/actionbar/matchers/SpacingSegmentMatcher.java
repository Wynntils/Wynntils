/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.SpacingSegment;
import com.wynntils.utils.type.StringReader;

/**
 * Represents a spacing segment in the action bar.
 * <p>This matcher is only used for spacings that are applied to the whole action bar.
 * Some segments have additional spacing on their own, which is matched by the segment itself.</p>
 */
public class SpacingSegmentMatcher implements ActionBarSegmentMatcher {
    private final int length;

    public SpacingSegmentMatcher(int length) {
        this.length = length;
    }

    @Override
    public ActionBarSegment read(StringReader reader) {
        // Read the spacing segment from the reader
        return new SpacingSegment(reader.read(length));
    }
}

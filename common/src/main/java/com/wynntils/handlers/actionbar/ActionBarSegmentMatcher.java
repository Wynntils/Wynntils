/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.text.StyledText;

public interface ActionBarSegmentMatcher {
    /**
     * Parse the given action bar styled text and return the corresponding segment.
     * The segment can be found anywhere in the text, and is not required to be at the start.
     * Implementations should use {@link StyledText#getStringWithoutFormatting()} to obtain
     * the plain text for regex matching, as segment boundaries may span multiple styled text parts.
     * @param actionBar The action bar styled text to parse
     * @return The parsed segment, or null if the text does not match the segment
     */
    ActionBarSegment parse(StyledText actionBar);
}

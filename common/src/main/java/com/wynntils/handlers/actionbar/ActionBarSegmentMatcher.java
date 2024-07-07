/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.utils.type.StringReader;

public interface ActionBarSegmentMatcher {
    ActionBarSegment read(StringReader reader);
}

/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class SpellSegment extends ActionBarSegment {
    public SpellSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "SpellSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}

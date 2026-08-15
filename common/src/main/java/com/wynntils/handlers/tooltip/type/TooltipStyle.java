/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.stats.type.StatListOrdering;

public record TooltipStyle(
        StatListOrdering ordering,
        boolean groupIdentifications,
        boolean showBestValueLastAlways,
        boolean rainbowInternalRoll,
        boolean showRollWheel) {}

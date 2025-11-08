/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.stats.type.StatListOrdering;

public record TooltipStyle(
        StatListOrdering identificationOrdering,
        boolean useDelimiters,
        boolean showBestValueLastAlways,
        boolean showStars,
        boolean showMaxValue // this only applies to crafted items
        ) {}

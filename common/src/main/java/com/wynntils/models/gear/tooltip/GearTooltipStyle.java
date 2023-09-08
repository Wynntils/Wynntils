/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.tooltip;

import com.wynntils.models.stats.type.StatListOrdering;

public record GearTooltipStyle(
        StatListOrdering identificationOrdering,
        boolean useDelimiters,
        boolean showBestValueLastAlways,
        boolean showStars) {}

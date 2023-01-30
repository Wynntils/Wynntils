/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.models.stats.type.StatListOrdering;

public record GearTooltipStyle(
        StatListOrdering reorder, boolean group, boolean showBestValueLastAlways, boolean showStars) {}

/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.stopwatch;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class StopwatchOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{if_str(stopwatch_zero;\"\";concat(if_str(stopwatch_running;\"\";\"&e\");leading_zeros(stopwatch_hours;2);\":\";leading_zeros(stopwatch_minutes;2);\":\";leading_zeros(stopwatch_seconds;2);\".\";leading_zeros(stopwatch_milliseconds;3)))}";

    public StopwatchOverlay() {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.BOTTOM_LEFT),
                new OverlaySize(100, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getPreviewTemplate() {
        return "01:24:31.877";
    }
}

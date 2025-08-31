/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunTrialsOverlay extends TextOverlay {
    private static final String TEMPLATE =
            """
                    {concat(
                        if_string(string_equals(lootrun_trial(0); "Unknown");"";concat("\\n§#00f010ffTrial #1: §7"; lootrun_trial(0)));
                        if_string(string_equals(lootrun_trial(1); "Unknown");"";concat("\\n§#00f010ffTrial #2: §7"; lootrun_trial(1)))
                    )}
                    """;

    private static final String PREVIEW_TEMPLATE =
            """
                    §#00f010ffTrial #1: §7All In
                    §#00f010ffTrial #2: §7Side Hustle
                    """;

    public LootrunTrialsOverlay() {
        super(
                new OverlayPosition(
                        55,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                new OverlaySize(130, 60),
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP);
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return PREVIEW_TEMPLATE;
    }

    @Override
    public boolean isVisible() {
        return Models.Lootrun.getState() != LootrunningState.NOT_RUNNING;
    }
}

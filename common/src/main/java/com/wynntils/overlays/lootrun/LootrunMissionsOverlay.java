/*
 * Copyright © Wynntils 2023-2025.
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

public class LootrunMissionsOverlay extends TextOverlay {
    private static final String TEMPLATE =
            """
                    {concat(
                        if_string(string_equals(lootrun_mission(0; false); "Unknown");"";concat("\\n§7Mission #1: "; lootrun_mission(0; true)));
                        if_string(string_equals(lootrun_mission(1; false); "Unknown");"";concat("\\n§7Mission #2: "; lootrun_mission(1; true)));
                        if_string(string_equals(lootrun_mission(2; false); "Unknown");"";concat("\\n§7Mission #3: "; lootrun_mission(2; true)));
                        if_string(string_equals(lootrun_mission(3; false); "Unknown");"";concat("\\n§7Mission #4: "; lootrun_mission(3; true)))
                    )}
                    """;

    private static final String PREVIEW_TEMPLATE =
            """
                    §7Mission #1: §eHigh Roller
                    §7Mission #2: §5Equilibrium
                    §7Mission #3: §cRedemption
                    §7Mission #4: §9Orphion's Grace
                    """;

    public LootrunMissionsOverlay() {
        super(
                new OverlayPosition(
                        75,
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

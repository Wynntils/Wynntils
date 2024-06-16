/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunMissionsOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{if_string(string_equals(lootrun_state;\"NOT_RUNNING\");\"\";concat(\"§7Mission #1: \";lootrun_mission(0);\"\\n§7Mission #2: \";lootrun_mission(1);\"\\n§7Mission #3: \";lootrun_mission(2);\"\\n§7Mission #4: \";lootrun_mission(3)))}";

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
                        160 + McUtils.mc().font.lineHeight,
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                new OverlaySize(130, 60),
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE);
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return PREVIEW_TEMPLATE;
    }
}

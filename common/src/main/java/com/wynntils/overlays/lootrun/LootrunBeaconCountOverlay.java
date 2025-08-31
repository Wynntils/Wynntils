/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunBeaconCountOverlay extends TextOverlay {
    private static final String TEMPLATE =
            """
            {concat(
            "§eYellow: ";string(lootrun_beacon_count("YELLOW"));"
            §9Blue: ";string(lootrun_beacon_count("BLUE"));"
            §5Purple: ";string(lootrun_beacon_count("PURPLE"));"
            §7Gray: ";string(lootrun_beacon_count("GRAY"));"/3";"
            §6Orange: ";string(lootrun_beacon_count("ORANGE"));" (+";string(lootrun_orange_beacon_count);") (";string(lootrun_next_orange_expire);")";"
            §bAqua: ";string(lootrun_beacon_count("AQUA"));"
            §8Dark Gray: ";string(lootrun_beacon_count("DARK_GRAY"));"/1";"
            §aGreen: ";string(lootrun_beacon_count("GREEN"));"
            §cRed: ";string(lootrun_beacon_count("RED"));" (";string(lootrun_red_beacon_challenge_count);")";"
            §fWhite: ";string(lootrun_beacon_count("WHITE"));"/1";"
            §#00f010ffCrimson: ";string(lootrun_beacon_count("CRIMSON"));"/2";"
            §#00f000ffRainbow: ";string(lootrun_beacon_count("RAINBOW"));" (";string(lootrun_rainbow_beacon_count);")")}
            """;

    private static final String PREVIEW_TEMPLATE =
            """
            §eYellow: 0
            §9Blue: 0
            §5Purple: 0
            §7Gray: 0/3
            §6Orange: 0 (+1) (5)
            §bAqua: 0
            §8Dark Gray: 0/1
            §aGreen: 0
            §cRed: 0 (0)
            §fWhite: 0/1
            §#00f010ffCrimson: 0/2
            §#00f000ffRainbow: 0 (10)
            """;

    public LootrunBeaconCountOverlay() {
        super(
                new OverlayPosition(
                        120,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                250,
                300);
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

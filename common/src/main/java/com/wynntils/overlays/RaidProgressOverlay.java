/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class RaidProgressOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{concat(\"§6§l§n\";current_raid;\"\\n\\n§dChallenge 1: \";if_str(eq(raid_room_time(\"challenge_1\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(raid_room_time(\"challenge_1\");60));2);\":\";leading_zeros(int(mod(raid_room_time(\"challenge_1\");60));2)));\"\\n§dChallenge 2: \";if_str(eq(raid_room_time(\"challenge_2\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(raid_room_time(\"challenge_2\");60));2);\":\";leading_zeros(int(mod(raid_room_time(\"challenge_2\");60));2)));\"\\n§dChallenge 3: \";if_str(eq(raid_room_time(\"challenge_3\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(raid_room_time(\"challenge_3\");60));2);\":\";leading_zeros(int(mod(raid_room_time(\"challenge_3\");60));2)));\"\\n\\n§4Boss: \";if_str(eq(raid_room_time(\"boss_fight\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(raid_room_time(\"boss_fight\");60));2);\":\";leading_zeros(int(mod(raid_room_time(\"boss_fight\");60));2)));\"\\n\\n§5Total: \";concat(\"§b\";leading_zeros(int(div(raid_time;60));2);\":\";leading_zeros(int(mod(raid_time;60));2)))}";

    private static final String PREVIEW_TEMPLATE =
            """
            §6§l§nNest of the Grootslangs

            §dChallenge 1: §b01:17
            §dChallenge 2: §b02:04
            §dChallenge 3: §7--:--

            §4Boss: §7--:--

            §5Total: §b03:36
            """;

    public RaidProgressOverlay() {
        super(
                new OverlayPosition(
                        60,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                150,
                120);
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
    public boolean isRenderedDefault() {
        return Models.Raid.getCurrentRaid() != null;
    }
}

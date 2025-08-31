/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class TowerStatsOverlay extends TextOverlay {
    private static final String TEMPLATE =
            """
                    §9✦ War Info: §3[§9{format_duration(time_in_war)}§3]
                    §f❖ Tower EHP: §b{tower_effective_hp:F0}
                    §f❖ Tower DPS: §b{range_low(tower_dps):F0}§8 - §b{range_high(tower_dps):F0}
                    §f❖ Team DPS/1s: §c{team_dps(long(1)):F0}
                    §f❖ Team DPS/5s: §c{team_dps(long(5)):F0}
                    §f❖ Total Team DPS: §e{team_dps:F0}
                    §f❖ Estimated Time: §a{if_str(eq(estimated_time_to_finish_war;-1);"-";concat(str(int(estimated_time_to_finish_war));"s"))}
                    """;

    private static final String PREVIEW_TEMPLATE =
            """
                    §9✦ War Info: §3[§923s§3]
                    §f❖ Tower EHP: §b12000
                    §f❖ Tower DPS: §b1200§8 - §b2000
                    §f❖ Team DPS/1s: §c1332
                    §f❖ Team DPS/5s: §c1522
                    §f❖ Total Team DPS: §e1888
                    §f❖ Estimated Time: §a10s
                    """;

    public TowerStatsOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        5,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                125,
                70);
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
        return Models.GuildWarTower.getWarBattleInfo().isPresent();
    }
}

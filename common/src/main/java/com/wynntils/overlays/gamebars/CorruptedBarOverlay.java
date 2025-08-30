/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class CorruptedBarOverlay extends BaseBarOverlay {
    public CorruptedBarOverlay() {
        super(
                new OverlayPosition(
                        -70,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.PURPLE);
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.corruptedBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return CorruptedBar.class;
    }

    @Override
    public String icon() {
        return "☠";
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.corruptedBar.isActive();
    }
}

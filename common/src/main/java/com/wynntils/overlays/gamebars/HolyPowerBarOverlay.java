/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class HolyPowerBarOverlay extends BaseBarOverlay {
    public HolyPowerBarOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.DARK_AQUA);
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.holyPowerBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return HolyPowerBar.class;
    }

    @Override
    public String icon() {
        return "🗲";
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.holyPowerBar.isActive();
    }
}

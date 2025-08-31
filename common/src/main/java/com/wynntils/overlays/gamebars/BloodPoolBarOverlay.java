/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class BloodPoolBarOverlay extends HealthBarOverlay {
    public BloodPoolBarOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21));
    }

    @Override
    public String icon() {
        return "⚕";
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.bloodPoolBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return BloodPoolBar.class;
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.bloodPoolBar.isActive();
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        // Do not call super
    }
}

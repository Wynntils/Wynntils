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
import com.wynntils.models.abilities.bossbars.OphanimBar;
import com.wynntils.models.abilities.type.OphanimOrb;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.stream.Collectors;

public class OphanimBarOverlay extends BaseBarOverlay {
    public OphanimBarOverlay() {
        super(
                new OverlayPosition(
                        -70,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.LIGHT_BLUE);
    }

    @Override
    protected BossBarProgress progress() {
        return Models.Ability.ophanimBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return OphanimBar.class;
    }

    @Override
    protected boolean isVisible() {
        return Models.Ability.ophanimBar.isActive();
    }

    @Override
    protected String text() {
        return Models.Ability.ophanimBar.getHealed() + "% ❤ - "
                + Models.Ability.ophanimBar.getOrbs().stream()
                        .map(OphanimOrb::getString)
                        .collect(Collectors.joining());
    }
}

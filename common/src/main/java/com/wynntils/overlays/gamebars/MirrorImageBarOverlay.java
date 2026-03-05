/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.MirrorImageBar;
import com.wynntils.models.abilities.type.MirrorImageClone;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.stream.Collectors;

public class MirrorImageBarOverlay extends BaseBarOverlay {
    public MirrorImageBarOverlay() {
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
    protected BossBarProgress progress() {
        return Models.Ability.mirrorImageBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return MirrorImageBar.class;
    }

    @Override
    protected boolean isVisible() {
        return Models.Ability.mirrorImageBar.isActive();
    }

    @Override
    protected String text() {
        return Models.Ability.mirrorImageBar.getDuration()
                + "s "
                + Models.Ability.mirrorImageBar.getClones().stream()
                        .map(MirrorImageClone::getString)
                        .collect(Collectors.joining());
    }
}

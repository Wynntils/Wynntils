/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class CombatExperienceOverlay extends TextOverlay {
    @Persisted
    private final Config<Boolean> useShortFormat = new Config<>(true);

    public CombatExperienceOverlay() {
        super(
                new OverlayPosition(
                        -68,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(200, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @Override
    public String getTemplate() {
        String xpFormat = useShortFormat.get() ? "format_capped(capped_xp)" : "capped_xp";
        return Models.CombatXp.getCombatLevel().isAtCap() ? "" : "&2[&a{" + xpFormat + "} &6({xp_pct:1}%)&2]";
    }

    @Override
    public String getPreviewTemplate() {
        return "&2[&a54321/2340232&2 &6(2.3%)]";
    }
}

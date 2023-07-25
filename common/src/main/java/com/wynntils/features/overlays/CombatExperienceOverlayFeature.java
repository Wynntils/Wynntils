/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.RenderState;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

@ConfigCategory(Category.OVERLAYS)
public class CombatExperienceOverlayFeature extends Feature {
    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay combatExperienceOverlay = new CombatExperienceOverlay();

    public static class CombatExperienceOverlay extends TextOverlay {
        @RegisterConfig
        private final Config<Boolean> useShortFormat = new Config<>(true);

        protected CombatExperienceOverlay() {
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
}

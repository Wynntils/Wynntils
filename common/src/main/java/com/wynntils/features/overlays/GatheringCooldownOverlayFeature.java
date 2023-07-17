/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.OverlaySize;
import com.wynntils.core.features.overlays.RenderState;
import com.wynntils.core.features.overlays.TextOverlay;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

@ConfigCategory(Category.OVERLAYS)
public class GatheringCooldownOverlayFeature extends Feature {
    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay gatheringCooldownOverlay = new GatheringCooldownOverlay();

    public static class GatheringCooldownOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{if_str(gt(gathering_cooldown; 0);string(gathering_cooldown);\"\")}s gathering cooldown";

        protected GatheringCooldownOverlay() {
            super(
                    new OverlayPosition(
                            165,
                            -5,
                            VerticalAlignment.TOP,
                            HorizontalAlignment.RIGHT,
                            OverlayPosition.AnchorSection.TOP_RIGHT),
                    new OverlaySize(130, 20),
                    HorizontalAlignment.RIGHT,
                    VerticalAlignment.MIDDLE);
        }

        @Override
        public String getTemplate() {
            return TEMPLATE;
        }

        @Override
        public String getPreviewTemplate() {
            return TEMPLATE;
        }
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.RenderState;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayGroup;
import com.wynntils.mc.event.RenderEvent;
import java.util.ArrayList;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends Feature {
    @OverlayGroup(instances = 7, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<InfoBoxOverlay> infoBoxOverlays = new ArrayList<>();

    public static class InfoBoxOverlay extends TextOverlay {
        @RegisterConfig
        public final Config<String> content = new Config<>("");

        public InfoBoxOverlay(int id) {
            super(id);
        }

        @Override
        public String getTemplate() {
            return content.get();
        }

        @Override
        public String getPreviewTemplate() {
            if (!content.get().isEmpty()) {
                return content.get();
            }

            return "&cX: {x(my_loc):0}, &9Y: {y(my_loc):0}, &aZ: {z(my_loc):0}";
        }
    }
}

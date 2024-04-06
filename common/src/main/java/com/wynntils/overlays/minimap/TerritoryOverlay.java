/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minimap;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class TerritoryOverlay extends TextOverlay {
    private static final String TEMPLATE = "{territory}";

    public TerritoryOverlay() {
        super(
                new OverlayPosition(
                        140 + McUtils.mc().font.lineHeight,
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(130, 20),
                HorizontalAlignment.CENTER,
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

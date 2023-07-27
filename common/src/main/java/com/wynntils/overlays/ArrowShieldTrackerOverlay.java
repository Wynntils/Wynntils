/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class ArrowShieldTrackerOverlay extends TextOverlay {
    private static final String ARROW_SYMBOL = " ⬈"; // leading space is on purpose

    private static final String TEMPLATE =
            "{IF_STRING(GT(ARROW_SHIELD_COUNT; 0); CONCAT(\"Arrow Shield: \"; REPEAT(\"%s\"; ARROW_SHIELD_COUNT)); \"\")}"
                    .formatted(ARROW_SYMBOL);

    @RegisterConfig
    public final Config<CustomColor> textColor = new Config<>(CommonColors.LIGHT_BLUE);

    public ArrowShieldTrackerOverlay() {
        super(
                new OverlayPosition(
                        120,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(120, 14));
    }

    @Override
    public CustomColor getRenderColor() {
        return textColor.get();
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getPreviewTemplate() {
        return "Arrow Shield: {REPEAT(\"%s\"; 3)}".formatted(ARROW_SYMBOL);
    }
}

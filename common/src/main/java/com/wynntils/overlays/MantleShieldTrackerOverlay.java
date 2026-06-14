/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class MantleShieldTrackerOverlay extends TextOverlay {
    private static final String MANTLE_SYMBOL = " ⯃"; // leading space is on purpose

    private static final String TEMPLATE =
            "{IF_STRING(OR(GT(MANTLE_SHIELD_COUNT; 0); GT(BROKEN_MANTLE_SHIELD_COUNT; 0)); CONCAT(\"Mantle Shield:§9\"; REPEAT(\"%s\"; MANTLE_SHIELD_COUNT); \"§c\"; REPEAT(\"%s\"; BROKEN_MANTLE_SHIELD_COUNT)); \"\")}"
                    .formatted(MANTLE_SYMBOL, MANTLE_SYMBOL);

    @Persisted
    private final Config<CustomColor> textColor = new Config<>(CommonColors.PURPLE);

    public MantleShieldTrackerOverlay() {
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
        return "Mantle Shield:§9{REPEAT(\"%s\"; 4)}§c{REPEAT(\"%s\"; 2)}".formatted(MANTLE_SYMBOL, MANTLE_SYMBOL);
    }
}

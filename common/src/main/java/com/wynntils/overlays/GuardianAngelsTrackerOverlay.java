/*
 * Copyright © Wynntils 2023-2025.
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

public class GuardianAngelsTrackerOverlay extends TextOverlay {
    private static final String GUARDIAN_ANGELS_SYMBOL = " 🏹"; // leading space is on purpose

    private static final String TEMPLATE =
            "{IF_STRING(AND(GT(GUARDIAN_ANGELS_COUNT; 0); EQ_STR(SHIELD_TYPE;\"Guardian Angels\")); CONCAT(\"Guardian Angels: \"; REPEAT(\"%s\"; GUARDIAN_ANGELS_COUNT)); \"\")}"
                    .formatted(GUARDIAN_ANGELS_SYMBOL);

    @Persisted
    private final Config<CustomColor> textColor = new Config<>(CommonColors.YELLOW);

    public GuardianAngelsTrackerOverlay() {
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
        return "Guardian Angels: {REPEAT(\"%s\"; 2)}".formatted(GUARDIAN_ANGELS_SYMBOL);
    }
}

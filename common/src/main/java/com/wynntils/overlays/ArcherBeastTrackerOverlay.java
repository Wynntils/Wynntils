/*
 * Copyright © Wynntils 2026.
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

public class ArcherBeastTrackerOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{IF(GREATER_THAN(HOUNDS_TIME_LEFT;0);CONCAT(\"Hounds: \";STRING(HOUNDS_TIME_LEFT);\"s\");\"\")}"
                    + "{IF(GREATER_THAN(CROW_COUNT;0);CONCAT(\"\\nCrows: \";STRING(CROW_COUNT));\"\")}"
                    + "{IF(GREATER_THAN(SNAKE_COUNT;0);CONCAT(\"\\nSnakes: \";STRING(SNAKE_COUNT));\"\")}";

    @Persisted
    private final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

    public ArcherBeastTrackerOverlay() {
        super(
                new OverlayPosition(
                        275,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(120, 27));
    }

    @Override
    public CustomColor getRenderColor() {
        return textColor.get();
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return "Hounds: 17s\nCrows: 2\nSnakes: 1";
    }
}

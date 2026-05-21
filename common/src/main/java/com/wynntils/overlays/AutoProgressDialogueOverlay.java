/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

import java.util.function.BooleanSupplier;

public class AutoProgressDialogueOverlay extends TextOverlay {
    private static final String TEMPLATE = "§aAuto Processing Dialogue...";

    private final BooleanSupplier visible;

    public AutoProgressDialogueOverlay(BooleanSupplier visible) {
        super(
                new OverlayPosition(
                        30,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(160, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);

        this.visible = visible;
        fitText.store(true);
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return TEMPLATE;
    }

    @Override
    protected boolean isVisible() {
        return visible.getAsBoolean();
    }
}

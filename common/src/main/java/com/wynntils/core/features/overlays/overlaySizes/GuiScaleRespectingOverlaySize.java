/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.overlaySizes;

import com.wynntils.mc.utils.McUtils;

public class GuiScaleRespectingOverlaySize extends OverlaySize {
    public GuiScaleRespectingOverlaySize(float width, float height) {
        super(width, height);
    }

    @Override
    public float getWidth() {
        return (float) (this.width * McUtils.window().getGuiScale());
    }

    @Override
    public float getHeight() {
        return (float) (this.height * McUtils.window().getGuiScale());
    }
}

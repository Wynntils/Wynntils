/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

import com.wynntils.mc.utils.McUtils;

// Since we use guiScaledWidth/guiScaledHeight for Overlays, we do not need to factor in GUI scale here.
public class GuiScaledOverlaySize extends OverlaySize {
    public GuiScaledOverlaySize(float width, float height) {
        super(width, height);
    }

    @Override
    public float getWidth() {
        return this.width;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public float getRenderedWidth() {
        return (float) (getWidth() * McUtils.window().getGuiScale());
    }

    @Override
    public float getRenderedHeight() {
        return (float) (getHeight() * McUtils.window().getGuiScale());
    }
}

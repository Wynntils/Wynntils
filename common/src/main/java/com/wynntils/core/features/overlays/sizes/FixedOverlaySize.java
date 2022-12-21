/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

import com.wynntils.mc.utils.McUtils;

// We need to divide by the GUI scale to get the fixed size, since we use guiScaledWidth/guiScaledHeight for Overlays.
public abstract class FixedOverlaySize extends OverlaySize {
    protected FixedOverlaySize() {
        super();
    }

    protected FixedOverlaySize(float width, float height) {
        super(width, height);
    }

    protected FixedOverlaySize(String string) {
        super(string);
    }

    @Override
    public float getWidth() {
        return (float) (this.width / McUtils.guiScale());
    }

    @Override
    public float getHeight() {
        return (float) (this.height / McUtils.guiScale());
    }
}

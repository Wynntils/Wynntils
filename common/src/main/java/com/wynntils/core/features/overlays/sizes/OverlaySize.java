/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

public abstract class OverlaySize {
    protected float width;
    protected float height;

    public OverlaySize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public abstract float getWidth();

    public abstract float getHeight();

    public abstract float getRenderedWidth();

    public abstract float getRenderedHeight();
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

import java.util.Objects;

public class RestrictedRangeOverlaySize extends FixedOverlaySize {
    public RestrictedRangeOverlaySize() {
        super();
        this.maxWidth = -1;
        this.maxHeight = -1;
    }

    private final float maxWidth;
    private final float maxHeight;

    public RestrictedRangeOverlaySize(float width, float height, float maxWidth, float maxHeight) {
        super(width, height);

        this.width = Math.min(maxWidth, width);
        this.height = Math.min(maxHeight, height);

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setWidth(float newWidth) {
        this.width = Math.min(newWidth, maxWidth);
    }

    public void setHeight(float newHeight) {
        this.height = Math.min(newHeight, maxHeight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RestrictedRangeOverlaySize that = (RestrictedRangeOverlaySize) o;
        return Float.compare(that.maxWidth, maxWidth) == 0 && Float.compare(that.maxHeight, maxHeight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxWidth, maxHeight);
    }
}

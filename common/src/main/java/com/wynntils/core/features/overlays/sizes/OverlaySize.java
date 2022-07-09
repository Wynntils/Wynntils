/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OverlaySize implements Serializable {
    protected static final Pattern SIZE_REGEX = Pattern.compile("OverlaySize\\{width=(.+),height=(.+)}");

    protected float width;
    protected float height;

    // For GSON
    public OverlaySize() {}

    public OverlaySize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public OverlaySize(String string) {
        Matcher matcher = SIZE_REGEX.matcher(string.replaceAll(" ", ""));

        if (!matcher.matches()) {
            throw new RuntimeException("Failed to parse OverlaySize");
        }

        try {
            this.width = Float.parseFloat(matcher.group(1));
            this.height = Float.parseFloat(matcher.group(2));
        } catch (NumberFormatException exception) {
            throw new RuntimeException("Failed to parse OverlaySize");
        }
    }

    public abstract float getWidth();

    public abstract float getHeight();

    public abstract float getRenderedWidth();

    public abstract float getRenderedHeight();

    @Override
    public String toString() {
        return "OverlaySize{" + "width=" + width + ", height=" + height + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlaySize that = (OverlaySize) o;
        return Float.compare(that.width, width) == 0 && Float.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}

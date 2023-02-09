/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.sizes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OverlaySize {
    private static final Pattern SIZE_REGEX = Pattern.compile("OverlaySize\\{width=(.+),height=(.+)}");
    private static final float MINIMUM_HEIGHT = 3f;
    private static final float MINIMUM_WIDTH = 3f;

    protected float width;
    protected float height;

    // For GSON
    protected OverlaySize() {}

    protected OverlaySize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    protected OverlaySize(String string) {
        Matcher matcher = SIZE_REGEX.matcher(string.replace(" ", ""));

        if (!matcher.matches()) {
            throw new RuntimeException("Failed to parse OverlaySize");
        }

        try {
            this.width = Float.parseFloat(matcher.group(1));
            this.height = Float.parseFloat(matcher.group(2));
        } catch (NumberFormatException exception) {
            throw new RuntimeException("Failed to parse OverlaySize", exception);
        }
    }

    public abstract float getWidth();

    public abstract float getHeight();

    public void setWidth(float newWidth) {
        this.width = Math.max(newWidth, MINIMUM_WIDTH);
    }

    public void setHeight(float newHeight) {
        this.height = Math.max(newHeight, MINIMUM_HEIGHT);
    }

    @Override
    public String toString() {
        return "OverlaySize{" + "width=" + width + ", height=" + height + '}';
    }
}

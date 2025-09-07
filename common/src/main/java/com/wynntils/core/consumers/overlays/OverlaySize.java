/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the size of an overlay.
 * <p>
 * The size is represented by a width and a height and scales with GUI scale.
 */
public class OverlaySize {
    private static final Pattern SIZE_REGEX = Pattern.compile("OverlaySize\\{width=(.+),height=(.+)}");
    private static final float MINIMUM_HEIGHT = 3f;
    private static final float MINIMUM_WIDTH = 3f;

    private float width;
    private float height;

    // For GSON
    public OverlaySize() {}

    public OverlaySize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * This String-based constructor is implicitly called from {@link Config#tryParseStringValue}.
     */
    public OverlaySize(String string) {
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

    public OverlaySize copy() {
        return new OverlaySize(getWidth(), getHeight());
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

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

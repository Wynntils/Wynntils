/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.type;

// It is not valid to have includeBasicFormatting false and any other field true.
public record StyleType(
        boolean includeBasicFormatting, boolean includeEvents, boolean includeShadowColors, boolean includeFonts) {
    public StyleType {
        if (!includeBasicFormatting && (includeEvents || includeFonts)) {
            throw new IllegalArgumentException("Cannot have includeBasicFormatting=false and any other field true.");
        }
    }

    public static final StyleType NONE = new StyleType(false, false, false, false);
    public static final StyleType DEFAULT = new StyleType(true, false, false, false);
    public static final StyleType INCLUDE_SHADOW_COLORS = new StyleType(true, false, true, false);
    public static final StyleType INCLUDE_FONTS = new StyleType(true, false, false, true);
    public static final StyleType INCLUDE_SPECIALS = new StyleType(true, false, true, true);
    public static final StyleType INCLUDE_EVENTS = new StyleType(true, true, false, false);
    public static final StyleType COMPLETE = new StyleType(true, true, true, true);
}

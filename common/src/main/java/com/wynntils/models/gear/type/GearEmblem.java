/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.Locale;

public record GearEmblem(GearEmblemShape gearEmblemShape, int variant) {
    private static final int VARIANT_COUNT = 6;

    public GearEmblem {
        if (variant < 1 || variant > VARIANT_COUNT) {
            throw new IllegalArgumentException("Unknown gear emblem variant: " + variant);
        }
    }

    public static GearEmblem fromString(String emblem) {
        String[] parts = emblem.split("_");
        GearEmblemShape gearEmblemShape = GearEmblemShape.valueOf(parts[0].toUpperCase(Locale.ROOT));
        int variant = parts.length == 1 ? 1 : Integer.parseInt(parts[1]);
        return new GearEmblem(gearEmblemShape, variant);
    }

    public String getFrameCode() {
        return String.valueOf((char) ('\uE000' + (variant - 1) * 0x10 + gearEmblemShape.offset()));
    }

    public enum GearEmblemShape {
        DIAMOND(0),
        SQUARE(1),
        HEXAGON(2),
        SHIELD(3),
        CIRCLE(4),
        STICKER(5);

        private final int offset;

        GearEmblemShape(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import com.wynntils.models.gear.type.GearType;
import java.util.Locale;

public record ItemEmblem(ItemEmblemShape itemEmblemShape, int variant) {
    private static final int VARIANT_COUNT = 6;

    public ItemEmblem {
        if (variant < 1 || variant > VARIANT_COUNT) {
            throw new IllegalArgumentException("Unknown item emblem variant: " + variant);
        }
    }

    public static ItemEmblem fromString(String emblem) {
        String[] parts = emblem.split("_");
        ItemEmblemShape itemEmblemShape = ItemEmblemShape.valueOf(parts[0].toUpperCase(Locale.ROOT));
        int variant = parts.length == 1 ? 1 : Integer.parseInt(parts[1]);
        return new ItemEmblem(itemEmblemShape, variant);
    }

    public static ItemEmblem fromCraftedItem(GearType gearType) {
        ItemEmblemShape itemEmblemShape;

        if (gearType.isAccessory()) {
            itemEmblemShape = ItemEmblemShape.STICKER;
        } else if (gearType.isArmor()) {
            itemEmblemShape = ItemEmblemShape.SHIELD;
        } else {
            itemEmblemShape = ItemEmblemShape.DIAMOND;
        }

        return new ItemEmblem(itemEmblemShape, 4);
    }

    public String getFrameCode(boolean extraOffset) {
        return String.valueOf(
                (char) ((extraOffset ? '\uF000' : '\uE000') + (variant - 1) * 0x10 + itemEmblemShape.offset()));
    }

    public enum ItemEmblemShape {
        DIAMOND(0),
        SQUARE(1),
        HEXAGON(2), // Unconfirmed name
        SHIELD(3),
        BADGE(4), // Unconfirmed name
        STICKER(5);

        private final int offset;

        ItemEmblemShape(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }
}

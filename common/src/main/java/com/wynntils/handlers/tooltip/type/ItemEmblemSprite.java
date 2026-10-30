/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

public enum ItemEmblemSprite {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    BOW,
    DAGGER,
    WAND,
    RELIK,
    SPEAR,
    AXE,
    SCYTHE,
    PICKAXE,
    FISHING_ROD,
    RING,
    BRACELET,
    NECKLACE,
    MATERIAL,
    TRINKET,
    CRAFTED,
    NORMAL,
    UNIQUE,
    RARE,
    LEGENDARY,
    FABLED,
    MYTHIC,
    POTION,
    TOME,
    ASPECT,
    CHARM,
    SCROLL,
    FOOD,
    INGREDIENT,
    RUNE,
    WARD,
    MOUNT,
    // These 3 are currently unused, their names could be improved when their purpose is known
    SKULL,
    PARCHMENT,
    BOTTLE;

    public String getSpriteCode(boolean extraOffset) {
        return String.valueOf(
                (char) ((extraOffset ? '\uF000' : '\uE000') + (this.ordinal() / 9) * 0x10 + this.ordinal() % 9));
    }
}

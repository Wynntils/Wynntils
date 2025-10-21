/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.utils.render.Texture;

public enum QuickJumpButtonIcon {
    NONE(null),
    LIQUID_EMERALD(Texture.LIQUID_EMERALD_ICON),
    EMERALD(Texture.EMERALD_ICON),
    POTION(Texture.POTION_ICON),
    POWDER(Texture.POWDER_ICON),
    TOME(Texture.TOME_ICON),
    SCROLL(Texture.SCROLL_ICON),
    GEM(Texture.GEM_ICON),
    KEY(Texture.KEY_ICON),
    RING(Texture.RING_ICON),
    SWORD(Texture.SWORD_ICON),
    HELMET(Texture.HELMET_ICON),
    CHESTPLATE(Texture.CHESTPLATE_ICON),
    LEGGINGS(Texture.LEGGINGS_ICON),
    BOOTS(Texture.BOOTS_ICON),
    QUESTION_MARK(Texture.QUESTION_MARK_ICON),
    STAR(Texture.STAR_ICON);

    private final Texture texture;

    QuickJumpButtonIcon(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public QuickJumpButtonIcon next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }

    public QuickJumpButtonIcon prev() {
        int prevOrdinal = (this.ordinal() - 1 + values().length) % values().length;
        return values()[prevOrdinal];
    }
}

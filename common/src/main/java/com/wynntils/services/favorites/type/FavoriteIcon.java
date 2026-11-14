/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.favorites.type;

import com.wynntils.utils.render.Texture;

public enum FavoriteIcon {
    STAR(Texture.FAVORITE_STAR_ICON),
    SQUARE(Texture.FAVORITE_SQUARE_ICON),
    HOLLOW_CIRCLE(Texture.FAVORITE_HOLLOW_CIRCLE_ICON);

    private final Texture texture;

    FavoriteIcon(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }
}

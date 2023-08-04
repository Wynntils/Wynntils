/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.utils.render.Texture;

public enum LootrunTaskType {
    LOOT(Texture.SPELUNK),
    SLAY(Texture.SLAY),
    DESTROY(Texture.DESTROY),
    DEFEND(Texture.DEFEND);

    private final Texture texture;

    LootrunTaskType(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }
}

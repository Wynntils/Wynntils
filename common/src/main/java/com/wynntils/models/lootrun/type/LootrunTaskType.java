/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.utils.render.Texture;

public enum LootrunTaskType {
    LOOT(Texture.SPELUNK),
    SLAY(Texture.SLAY),
    TARGET(Texture.TARGET),
    DESTROY(Texture.DESTROY),
    DEFEND(Texture.DEFEND),
    UNKNOWN(Texture.WAYPOINT);

    private final Texture texture;

    LootrunTaskType(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }
}

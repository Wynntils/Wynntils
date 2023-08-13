/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import com.wynntils.utils.render.Texture;
import java.util.Arrays;

public enum CombatKind {
    BOSS_ALTARS("Boss Altars", Texture.BOSS_ALTAR),
    CAVES("Caves", Texture.CAVE),
    DUNGEONS("Dungeons", Texture.DUNGEON_ENTRANCE),
    GRIND_SPOTS("Grind Spots", Texture.GRIND_SPOT),
    RAIDS("Raids", Texture.RAID_ENTRANCE),
    RUNE_SHRINES("Rune Shrines", Texture.SHRINE);

    private final String name;
    private final Texture texture;

    CombatKind(String name, Texture texture) {
        this.name = name;
        this.texture = texture;
    }

    public String getName() {
        return name;
    }

    public Texture getIcon() {
        return texture;
    }

    public static CombatKind fromString(String str) {
        return Arrays.stream(values())
                .filter(kind -> kind.getName().equals(str))
                .findFirst()
                .orElse(null);
    }
}

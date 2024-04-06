/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import com.wynntils.utils.render.Texture;
import java.util.Arrays;

public enum CombatKind {
    BOSS_ALTARS("Boss Altars", Texture.BOSS_ALTAR, "boss-altar"),
    CAVES("Caves", Texture.CAVE, "cave"),
    DUNGEONS("Dungeons", Texture.DUNGEON_ENTRANCE, "dungeon"),
    GRIND_SPOTS("Grind Spots", Texture.GRIND_SPOT, "grind-spot"),
    RAIDS("Raids", Texture.RAID_ENTRANCE, "raid"),
    RUNE_SHRINES("Rune Shrines", Texture.SHRINE, "shrine");

    private final String name;
    private final Texture texture;
    private final String mapDataId;

    CombatKind(String name, Texture texture, String mapDataId) {
        this.name = name;
        this.texture = texture;
        this.mapDataId = mapDataId;
    }

    public String getName() {
        return name;
    }

    public Texture getIcon() {
        return texture;
    }

    public String getMapDataId() {
        return mapDataId;
    }

    public static CombatKind fromString(String str) {
        return Arrays.stream(values())
                .filter(kind -> kind.getName().equals(str))
                .findFirst()
                .orElse(null);
    }
}

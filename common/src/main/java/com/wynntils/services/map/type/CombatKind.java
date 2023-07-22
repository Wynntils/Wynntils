/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
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
    private final String serviceId;

    CombatKind(String name, Texture texture, String serviceId) {
        this.name = name;
        this.texture = texture;
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public String getServiceId() {
        return serviceId;
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

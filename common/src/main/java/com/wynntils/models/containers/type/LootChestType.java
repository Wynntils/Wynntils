/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.utils.render.Texture;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;

public enum LootChestType {
    TIER_1(Texture.CHEST_T1, "Loot Chest 1", Pattern.compile("Loot Chest §7\\[§f✫§8✫✫✫§7\\]")),
    TIER_2(Texture.CHEST_T2, "Loot Chest 2", Pattern.compile("Loot Chest §e\\[§6✫✫§8✫✫§e\\]")),
    TIER_3(Texture.CHEST_T3, "Loot Chest 3", Pattern.compile("Loot Chest §5\\[§d✫✫✫§8✫§5\\]")),
    TIER_4(Texture.CHEST_T4, "Loot Chest 4", Pattern.compile("Loot Chest §3\\[§b✫✫✫✫§3\\]"));

    private final Texture waypointTexture;
    private final String waypointName;
    private final Pattern titlePattern;

    LootChestType(Texture waypointTexture, String waypointName, Pattern titlePattern) {
        this.waypointTexture = waypointTexture;
        this.waypointName = waypointName;
        this.titlePattern = titlePattern;
    }

    public Texture getWaypointTexture() {
        return waypointTexture;
    }

    public String getWaypointName() {
        return waypointName;
    }

    public Pattern getTitlePattern() {
        return titlePattern;
    }

    public static LootChestType fromTitle(Screen screen) {
        for (LootChestType chestType : values()) {
            if (chestType
                    .getTitlePattern()
                    .matcher(screen.getTitle().getString())
                    .matches()) {
                return chestType;
            }
        }

        return null;
    }
}

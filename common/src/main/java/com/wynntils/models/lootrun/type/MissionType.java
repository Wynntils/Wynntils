/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;

public enum MissionType {
    UNKNOWN("Unknown", ChatFormatting.WHITE),
    FAILED("Failed", ChatFormatting.DARK_RED),

    CLEANSING_GREED("Cleansing Greed", ChatFormatting.YELLOW),
    HIGH_ROLLER("High Roller", ChatFormatting.YELLOW),
    MATERIALISM("Materialism", ChatFormatting.YELLOW),
    ORPHIONS_GRACE("Orphion's Grace", ChatFormatting.BLUE),
    CLEANSING_RITUAL("Cleansing Ritual", ChatFormatting.DARK_PURPLE),
    EQUILIBRIUM("Equilibrium", ChatFormatting.DARK_PURPLE),
    INNER_PEACE("Inner Peace", ChatFormatting.DARK_PURPLE),
    BACKUP_BEAT("Backup Beat", ChatFormatting.GREEN),
    STASIS("Stasis", ChatFormatting.GREEN),
    GAMBLING_BEAST("Gambling Beast", ChatFormatting.RED),
    REDEMPTION("Redemption", ChatFormatting.RED),
    ULTIMATE_SACRIFICE("Ultimate Sacrifice", ChatFormatting.RED);

    private final String name;
    private final ChatFormatting color;

    MissionType(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        return color + name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public static MissionType fromName(String name) {
        for (MissionType type : values()) {
            if (type == UNKNOWN || type == FAILED) continue;
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    public static List<MissionType> missionTypes() {
        return Arrays.stream(values())
                .filter(type -> type != UNKNOWN && type != FAILED)
                .toList();
    }
}

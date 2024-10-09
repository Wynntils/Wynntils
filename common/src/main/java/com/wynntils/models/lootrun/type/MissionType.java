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
    HOARDER("Hoarder", ChatFormatting.YELLOW),
    MATERIALISM("Materialism", ChatFormatting.YELLOW),
    TREASURE_HUNTING("Treasure Hunting", ChatFormatting.YELLOW),
    GOURMAND("Gourmand", ChatFormatting.BLUE),
    ORPHIONS_GRACE("Orphion's Grace", ChatFormatting.BLUE),
    PORPHYROPHOBIA("Porphyrophobia", ChatFormatting.BLUE),
    CHRONOKINESIS("Chronokinesis", ChatFormatting.BLUE),
    CLEANSING_RITUAL("Cleansing Ritual", ChatFormatting.DARK_PURPLE),
    EQUILIBRIUM("Equilibrium", ChatFormatting.DARK_PURPLE),
    INNER_PEACE("Inner Peace", ChatFormatting.DARK_PURPLE),
    COMPLETE_CHAOS("Complete Chaos", ChatFormatting.DARK_PURPLE),
    JESTERS_TRICK("Jester's Trick", ChatFormatting.DARK_PURPLE),
    BACKUP_BEAT("Backup Beat", ChatFormatting.GREEN),
    STASIS("Stasis", ChatFormatting.GREEN),
    SAFETY_SEEKER("Safety Seeker", ChatFormatting.GREEN),
    GAMBLING_BEAST("Gambling Beast", ChatFormatting.RED),
    REDEMPTION("Redemption", ChatFormatting.RED),
    ULTIMATE_SACRIFICE("Ultimate Sacrifice", ChatFormatting.RED),
    WARMTH_DEVOURER("Warmth Devourer", ChatFormatting.RED);

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

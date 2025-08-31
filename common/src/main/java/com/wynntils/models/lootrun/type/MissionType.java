/*
 * Copyright © Wynntils 2024-2025.
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
    HIGH_ROLLER("High Roller", ChatFormatting.YELLOW, 0, 1),
    HOARDER("Hoarder", ChatFormatting.YELLOW),
    MATERIALISM("Materialism", ChatFormatting.YELLOW),
    GOURMAND("Gourmand", ChatFormatting.BLUE),
    ORPHIONS_GRACE("Orphion's Grace", ChatFormatting.BLUE),
    PORPHYROPHOBIA("Porphyrophobia", ChatFormatting.BLUE),
    CHRONOKINESIS("Chronokinesis", ChatFormatting.BLUE),
    CLEANSING_RITUAL("Cleansing Ritual", ChatFormatting.DARK_PURPLE),
    EQUILIBRIUM("Equilibrium", ChatFormatting.DARK_PURPLE),
    INNER_PEACE("Inner Peace", ChatFormatting.DARK_PURPLE),
    COMPLETE_CHAOS("Complete Chaos", ChatFormatting.DARK_PURPLE),
    JESTERS_TRICK("Jester's Trick", ChatFormatting.DARK_PURPLE),
    INTEREST_SCHEME("Interest Scheme", ChatFormatting.DARK_PURPLE),
    BACKUP_BEAT("Backup Beat", ChatFormatting.GREEN),
    STASIS("Stasis", ChatFormatting.GREEN),
    OPTIMISM("Optimism", ChatFormatting.GREEN),
    REDEMPTION("Redemption", ChatFormatting.RED, 1, 0),
    THRILL_SEEKER("Thrill Seeker", ChatFormatting.RED);

    private final String name;
    private final ChatFormatting color;
    private final int sacrifices;
    private final int rerolls;

    MissionType(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
        this.sacrifices = 0;
        this.rerolls = 0;
    }

    MissionType(String name, ChatFormatting color, int sacrifices, int rerolls) {
        this.name = name;
        this.color = color;
        this.sacrifices = sacrifices;
        this.rerolls = rerolls;
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

    public int getSacrifices() {
        return sacrifices;
    }

    public int getRerolls() {
        return rerolls;
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

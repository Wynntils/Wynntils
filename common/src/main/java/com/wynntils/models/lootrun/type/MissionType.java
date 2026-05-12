/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;

public enum MissionType {
    // Order these the same way like https://wynncraft.wiki.gg/wiki/Lootrunning#Missions
    UNKNOWN("Unknown", ChatFormatting.WHITE),
    FAILED("Failed", ChatFormatting.DARK_RED),

    CLEANSING_GREED("Cleansing Greed", ChatFormatting.YELLOW),
    MATERIALISM("Materialism", ChatFormatting.YELLOW),
    HOARDER("Hoarder", ChatFormatting.YELLOW),
    JESTERS_TRICK("Jester's Trick", ChatFormatting.YELLOW),
    INTEREST_SCHEME("Interest Scheme", ChatFormatting.YELLOW),
    ORPHIONS_GRACE("Orphion's Grace", ChatFormatting.BLUE),
    OPAL_OFFERING("Opal Offering", ChatFormatting.BLUE),
    GOURMAND("Gourmand", ChatFormatting.BLUE),
    PORPHYROPHOBIA("Porphyrophobia", ChatFormatting.DARK_PURPLE),
    SACRIFICIAL_RITUAL("Sacrificial Ritual", ChatFormatting.DARK_PURPLE),
    RADIANT_HUNTER("Radiant Hunter", ChatFormatting.DARK_PURPLE),
    EQUILIBRIUM("Equilibrium", ChatFormatting.DARK_PURPLE),
    INNER_PEACE("Inner Peace", ChatFormatting.DARK_PURPLE),
    OPTIMISM("Optimism", ChatFormatting.GOLD),
    BACKUP_BEAT("Backup Beat", ChatFormatting.GOLD),
    REQUIEM("Requiem", ChatFormatting.GREEN),
    STASIS("Stasis", ChatFormatting.GREEN),
    CHRONOKINESIS("Chronokinesis", ChatFormatting.GREEN),
    THRILL_SEEKER("Thrill Seeker", ChatFormatting.RED),
    HIGH_ROLLER("High Roller", ChatFormatting.WHITE, 0, 1),
    REDEMPTION("Redemption", ChatFormatting.WHITE, 1, 0),
    COMPLETE_CHAOS("Complete Chaos", ChatFormatting.WHITE);

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

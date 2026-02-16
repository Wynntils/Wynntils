/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.utils.EnumUtils;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Dungeon {
    DECREPIT_SEWERS("Decrepit Sewers",
            new DungeonData(8, -946, -1886),
            new DungeonData(70, 3533, 2374)),
    INFESTED_PIT("Infested Pit",
            new DungeonData(17, -193, -1870),
            new DungeonData(74, 3424, 3510)),
    UNDERWORLD_CRYPT("Underworld Crypt",
            new DungeonData(23, 290, -1950),
            new DungeonData(82, 3313, 5346)),
    TIMELOST_SANCTUM("Time-Lost Sanctum",
            new DungeonData(27, -263, -1069),
            new DungeonData(0, 0, 0)), // TODO FIGURE OUT VALUES
    SAND_SWEPT_TOMB("Sand-Swept Tomb",
            new DungeonData(36, 1432, -1830),
            new DungeonData(86, 3331, 4184)),
    ICE_BARROWS("Ice Barrows",
            new DungeonData(45, 132, -636),
            new DungeonData(90, 2960, 8120)),
    UNDERGROWTH_RUINS("Undergrowth Ruins",
            new DungeonData(54, -641, -841),
            new DungeonData(94, 2865, 8995)),
    GALLEONS_GRAVEYARD("Galleon's Graveyard",
            new DungeonData(63, -582, -3511),
            new DungeonData(98, 4286, -18341)),
    FALLEN_FACTORY("Fallen Factory",
            new DungeonData(90, -1646, -2608),
            new DungeonData(0, 0, 0)), // TODO FIGURE OUT VALUES
    ELDRITCH_OUTLOOK("Eldritch Outlook",
            new DungeonData(100, 1291, -749),
            new DungeonData(0, 0, 0)), // TODO FIGURE OUT VALUES
    LOST_SANCTUARY(true, false);

    private final String name;
    private DungeonData dungeonData;
    private DungeonData corruptedDungeonData;

    Dungeon() {
        this(false, false);
    }

    Dungeon(boolean removed, boolean corruptedRemoved) {
        this.name = EnumUtils.toNiceString(name());
        this.dungeonData = new DungeonData(0, 0, 0, removed);
        this.corruptedDungeonData = new DungeonData(0, 0, 0, corruptedRemoved);
    }

    Dungeon(String name, DungeonData dungeonData, DungeonData corruptedDungeonData) {
        this.name = name;
        this.dungeonData = dungeonData;
        this.corruptedDungeonData = corruptedDungeonData;
    }

    public static Dungeon fromName(String name) {
        for (Dungeon dungeon : values()) {
            if (dungeon.getName().equals(name)) {
                return dungeon;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public DungeonData getDungeonData() {
        return dungeonData;
    }

    public DungeonData getCorruptedDungeonData() {
        return corruptedDungeonData;
    }

    public boolean isRemoved() {
        return dungeonData.isRemoved();
    }

    public boolean isCorruptedRemoved() {
        return corruptedDungeonData.isRemoved();
    }

    public String getInitials() {
        return Arrays.stream(name.split(" ", 2)).map(s -> s.substring(0, 1)).collect(Collectors.joining());
    }

    public static class DungeonData {
        private final int combatLevel;
        private final int xPos;
        private final int yPos;
        private final boolean removed;

        public DungeonData(int combatLevel, int xPos, int yPos) {
            this(combatLevel, xPos, yPos, false);
        }

        public DungeonData(int combatLevel, int xPos, int yPos, boolean removed) {
            this.combatLevel = combatLevel;
            this.xPos = xPos;
            this.yPos = yPos;
            this.removed = removed;
        }

        public int getCombatLevel() { return combatLevel; }
        public int getXPos() { return xPos; }
        public int getYPos() { return yPos; }
        public boolean isRemoved() { return removed; }

        @Override
        public String toString() {
            return "DungeonData{" +
                    "combatLevel=" + combatLevel +
                    ", xPos=" + xPos +
                    ", yPos=" + yPos +
                    ", removed=" + removed +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Dungeon{" +
                "name='" + name + '\'' +
                ", dungeonData=" + dungeonData +
                ", corruptedDungeonData=" + corruptedDungeonData +
                '}';
    }
}

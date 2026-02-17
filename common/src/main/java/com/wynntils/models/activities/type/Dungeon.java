/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

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
            new DungeonData(27, -263, -1069)),
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
            new DungeonData(90, -1646, -2608)),
    ELDRITCH_OUTLOOK("Eldritch Outlook",
            new DungeonData(100, 1291, -749)),
    LOST_SANCTUARY("Lost Sanctuary");

    private final String name;
    private final DungeonData dungeonData;
    private final DungeonData corruptedDungeonData;

    Dungeon(String name) {
        this.name = name;
        this.dungeonData = new DungeonData();
        this.corruptedDungeonData = new DungeonData();
    }

    Dungeon(String name, DungeonData dungeonData) {
        this.name = name;
        this.dungeonData = dungeonData;
        this.corruptedDungeonData = new DungeonData();
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

    public boolean isExists() {
        return dungeonData.isExists();
    }

    public boolean isCorruptedExists() {
        return corruptedDungeonData.isExists();
    }

    public String getInitials() {
        return Arrays.stream(name.split(" ", 2)).map(s -> s.substring(0, 1)).collect(Collectors.joining());
    }

    public static class DungeonData {
        private final int combatLevel;
        private final int xPos;
        private final int yPos;
        private final boolean exists;

        public DungeonData() {
            this(0, 0, 0, false);
        }

        public DungeonData(int combatLevel, int xPos, int yPos) {
            this(combatLevel, xPos, yPos, true);
        }

        public DungeonData(int combatLevel, int xPos, int yPos, boolean exists) {
            this.combatLevel = combatLevel;
            this.xPos = xPos;
            this.yPos = yPos;
            this.exists = exists;
        }

        public int getCombatLevel() { return combatLevel; }
        public int getXPos() { return xPos; }
        public int getYPos() { return yPos; }
        public boolean isExists() { return exists; }

        @Override
        public String toString() {
            return "DungeonData{" +
                    "combatLevel=" + combatLevel +
                    ", xPos=" + xPos +
                    ", yPos=" + yPos +
                    ", exists=" + exists +
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

/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Dungeon {
    DECREPIT_SEWERS(
            "Decrepit Sewers",
            Optional.of(new DungeonData(8, -946, -1886)),
            Optional.of(new DungeonData(70, 3533, 2374))),
    INFESTED_PIT(
            "Infested Pit",
            Optional.of(new DungeonData(17, -193, -1870)),
            Optional.of(new DungeonData(74, 3424, 3510))),
    UNDERWORLD_CRYPT(
            "Underworld Crypt",
            Optional.of(new DungeonData(23, 290, -1950)),
            Optional.of(new DungeonData(82, 3313, 5346))),
    TIMELOST_SANCTUM(
            "Timelost Sanctum",
            Optional.of(new DungeonData(27, -263, -1069)),
            Optional.empty()), // Corrupted version is in Lost Sanctuary
    LOST_SANCTUARY(
            "Lost Sanctuary",
            Optional.empty(), // Regular version is in Time-Lost Sanctum
            Optional.of(new DungeonData(78, 3025, 6429))),
    SAND_SWEPT_TOMB(
            "Sand-Swept Tomb",
            Optional.of(new DungeonData(36, 1432, -1830)),
            Optional.of(new DungeonData(86, 3331, 4184))),
    ICE_BARROWS(
            "Ice Barrows", Optional.of(new DungeonData(45, 132, -636)), Optional.of(new DungeonData(90, 2960, 8120))),
    UNDERGROWTH_RUINS(
            "Undergrowth Ruins",
            Optional.of(new DungeonData(54, -641, -841)),
            Optional.of(new DungeonData(94, 2865, 8995))),
    GALLEONS_GRAVEYARD(
            "Galleon's Graveyard",
            Optional.of(new DungeonData(63, -582, -3511)),
            Optional.of(new DungeonData(98, 4286, -18341))),
    FALLEN_FACTORY(
            "Fallen Factory",
            Optional.of(new DungeonData(90, -1646, -2608)),
            Optional.empty()), // Corrupted version does not exist
    ELDRITCH_OUTLOOK(
            "Eldritch Outlook",
            Optional.of(new DungeonData(100, 1291, -749)),
            Optional.empty()); // Corrupted version does not exist

    private final String name;
    private final Optional<DungeonData> dungeonData;
    private final Optional<DungeonData> corruptedDungeonData;

    Dungeon(String name, Optional<DungeonData> dungeonData, Optional<DungeonData> corruptedDungeonData) {
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

    public Optional<DungeonData> getDungeonData() {
        return dungeonData;
    }

    public Optional<DungeonData> getCorruptedDungeonData() {
        return corruptedDungeonData;
    }

    public boolean doesExist() {
        return dungeonData.isPresent();
    }

    public boolean doesCorruptedExist() {
        return corruptedDungeonData.isPresent();
    }

    public String getInitials() {
        return Arrays.stream(name.split(" ", 2)).map(s -> s.substring(0, 1)).collect(Collectors.joining());
    }

    public static class DungeonData {
        private final int combatLevel;
        private final int xPos;
        private final int zPos;

        public DungeonData(int combatLevel, int xPos, int yPos) {
            this.combatLevel = combatLevel;
            this.xPos = xPos;
            this.zPos = yPos;
        }

        public int getCombatLevel() {
            return combatLevel;
        }

        public int getXPos() {
            return xPos;
        }

        public int getZPos() {
            return zPos;
        }

        @Override
        public String toString() {
            return "DungeonData{" + "combatLevel=" + combatLevel + ", xPos=" + xPos + ", zPos=" + zPos + '}';
        }
    }

    @Override
    public String toString() {
        return "Dungeon{" + "name='"
                + name + '\'' + ", dungeonData="
                + dungeonData + ", corruptedDungeonData="
                + corruptedDungeonData + '}';
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Models;

public final class AbilityPointProgression {
    private static final int[] POINTS_AT_LEVEL = new int[121];

    static {
        int[] milestones = {
            1, 2, 4, 6, 8, 10, 12, 13, 15, 17,
            18, 20, 22, 23, 24, 26, 28, 30, 32, 34,
            37, 39, 41, 44, 46, 48, 50, 52, 54, 56,
            58, 60, 62, 64, 67, 70, 73, 76, 80, 84,
            88, 92, 96, 100, 104, 107, 110, 113, 116, 120
        };
        int[] points = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50
        };

        int milestoneIdx = 0;
        for (int level = 1; level <= 120; level++) {
            if (milestoneIdx < milestones.length && level == milestones[milestoneIdx]) {
                POINTS_AT_LEVEL[level] = points[milestoneIdx++];
            } else {
                POINTS_AT_LEVEL[level] = POINTS_AT_LEVEL[level - 1];
            }
        }
    }

    public static int getPointsAtLevel(int combatLevel) {
        if (combatLevel <= 0) return 0;
        return Math.min(50, POINTS_AT_LEVEL[Math.min(combatLevel, 120)] + getLoanedPoints());
    }

    public static int getLevelForPoints(int abilityPoints) {
        int earnedPoints = Math.min(abilityPoints, 50) - getLoanedPoints();
        if (earnedPoints <= 0) return 1;
        if (earnedPoints >= 50) return 120;

        for (int level = 1; level <= 120; level++) {
            if (POINTS_AT_LEVEL[level] >= earnedPoints) {
                return level;
            }
        }
        return 120;
    }

    public static int getTotalPointsForTree(AbilityTreeInfo info) {
        if (info == null || info.nodes() == null) return 0;
        return info.nodes().stream().mapToInt(AbilityTreeSkillNode::cost).sum();
    }

    private static int getLoanedPoints() {
        return switch (Models.Character.getRank()) {
            case VIP_PLUS -> 2;
            case HERO, HERO_PLUS, CHAMPION -> 4;
            default -> 0;
        };
    }
}

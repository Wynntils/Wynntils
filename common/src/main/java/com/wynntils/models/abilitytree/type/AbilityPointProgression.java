/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Models;

// If this needs update look at:
// https://wynncraft.wiki.gg/wiki/Ability_Tree#Ability_Points

public final class AbilityPointProgression {
    private static final int MAX_ABILITY_POINTS = 50;
    private static final int[] POINTS_AT_LEVEL = createPointsAtLevel();

    private static int[] createPointsAtLevel() {
        int maxLevel = Models.CombatXp.MAX_LEVEL;
        int[] milestones = {
            1, 2, 4, 6, 8, 10, 12, 13, 15, 17,
            18, 20, 22, 23, 24, 26, 28, 30, 32, 34,
            37, 39, 41, 44, 46, 48, 50, 52, 54, 56,
            58, 60, 62, 64, 67, 70, 73, 76, 80, 84,
            88, 92, 96, 100, 104, 107, 110, 113, 116, 120
        };
        int[] result = new int[maxLevel];
        int milestoneIdx = 0;
        for (int level = 1; level < maxLevel; level++) {
            if (milestoneIdx < milestones.length && level == milestones[milestoneIdx]) {
                ++milestoneIdx;
                result[level] = milestoneIdx;
            } else {
                result[level] = result[level - 1];
            }
        }
        return result;
    }

    public static int getPointsAtLevel(int combatLevel) {
        if (combatLevel <= 0) return 0;
        return Math.min(
                MAX_ABILITY_POINTS,
                POINTS_AT_LEVEL[Math.min(combatLevel, Models.CombatXp.MAX_LEVEL - 1)] + getLoanedPoints());
    }

    public static int getLevelForPoints(int abilityPoints) {
        int maxLevel = Models.CombatXp.MAX_LEVEL;
        int earnedPoints = Math.min(abilityPoints, MAX_ABILITY_POINTS) - getLoanedPoints();
        if (earnedPoints <= 0) return 1;
        if (earnedPoints >= MAX_ABILITY_POINTS) return maxLevel - 1;

        for (int level = 1; level < maxLevel; level++) {
            if (POINTS_AT_LEVEL[level] >= earnedPoints) {
                return level;
            }
        }
        return maxLevel - 1;
    }

    public static int getTotalPointsForTree(AbilityTreeInfo info) {
        if (info == null || info.nodes() == null) return 0;
        return info.nodes().stream().mapToInt(AbilityTreeSkillNode::cost).sum();
    }

    private static int getLoanedPoints() {
        return switch (Models.Account.getRank()) {
            case VIP_PLUS -> 2;
            case HERO, HERO_PLUS, CHAMPION -> 4;
            default -> 0;
        };
    }
}

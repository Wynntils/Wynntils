package com.wynntils.services.loadout.type;

import com.wynntils.models.abilitytree.type.ArchetypeType;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.utils.render.Texture;

public record Loadout(
        String name,
        LoadoutType type,
        SavableSkillPointSet skillPoints,
        SavableAbilityTree abilityTree,
        SavableAspectSet aspects,
        boolean favourited) {

    public boolean hasSkillPoints() {
        return skillPoints != null;
    }

    public boolean hasAbilityTree() {
        return abilityTree != null;
    }

    public boolean hasAspects() {
        return aspects != null;
    }

    public ClassType getClassType() {
        if (hasAbilityTree()) {
            ClassType atClass = abilityTree.classType();
            if (atClass != null && atClass != ClassType.NONE) {
                return atClass;
            }
        }
        if (hasAspects()) {
            ClassType aspectClass = aspects.classType();
            if (aspectClass != null && aspectClass != ClassType.NONE) {
                return aspectClass;
            }
        }
        return ClassType.NONE;
    }

    public boolean hasClassType() {
        return getClassType() != ClassType.NONE;
    }

    public String getMainArchetype() {
        return hasAbilityTree() ? abilityTree.getMainArchetype() : null;
    }

    public int getNodeCount() {
        return hasAbilityTree() ? abilityTree.getNodeCount() : 0;
    }

    public int getAspectCount() {
        return hasAspects() ? aspects.getAspectCount() : 0;
    }

    public int getMaxLevel() {
        int spLevel = hasSkillPoints() ? skillPoints.getMinimumCombatLevel() : 0;
        int atLevel = hasAbilityTree() ? abilityTree.getLevel() : 0;
        int asLevel = hasAspects() ? aspects.getLevel() : 0;

        return Math.max(1, Math.max(spLevel, Math.max(atLevel, asLevel)));
    }

    public int getSkillPointsSum() {
        return hasSkillPoints() ? skillPoints.getSkillPointsSum() : 0;
    }

    public MenuCategory getMenuCategory() {
        return switch (type) {
            case LoadoutType.BUILD -> MenuCategory.BUILD_LOADOUT;
            case LoadoutType.ABILITY_TREE -> MenuCategory.ABILITY_TREE_LOADOUT;
            case LoadoutType.SKILL_POINT -> MenuCategory.SKILL_POINT_LOADOUT;
            case LoadoutType.ASPECT -> MenuCategory.ASPECT_LOADOUT;
        };
    }

    public ArchetypeType getArchetypeType() {
        String archetype = getMainArchetype();
        if (archetype == null || archetype.isEmpty()) return null;

        return ArchetypeType.fromName(archetype);
    }

    public Texture getAspectTexture() {
        return switch (getClassType()) {
            case ClassType.ARCHER-> Texture.ASPECT_ARCHER;
            case ClassType.ASSASSIN -> Texture.ASPECT_ASSASSIN;
            case ClassType.MAGE -> Texture.ASPECT_MAGE;
            case ClassType.SHAMAN -> Texture.ASPECT_SHAMAN;
            case ClassType.WARRIOR -> Texture.ASPECT_WARRIOR;
            default -> null;
        };
    }

    public Texture getFlameTexture() {
        return switch (getClassType()) {
            case ClassType.ARCHER-> Texture.ASPECT_ARCHER_FLAME;
            case ClassType.ASSASSIN -> Texture.ASPECT_ASSASSIN_FLAME;
            case ClassType.MAGE -> Texture.ASPECT_MAGE_FLAME;
            case ClassType.SHAMAN -> Texture.ASPECT_SHAMAN_FLAME;
            case ClassType.WARRIOR -> Texture.ASPECT_WARRIOR_FLAME;
            default -> null;
        };
    }
}
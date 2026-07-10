/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.utils.render.Texture;

public record Loadout(
        String name,
        SavableSkillPointSet skillPoints,
        SavableAbilityTree abilityTree,
        SavableAspectSet aspect,
        LoadoutType type) {
    public boolean hasSkillPoints() {
        return skillPoints != null;
    }

    public boolean hasAbilityTree() {
        return abilityTree != null;
    }

    public boolean hasAspects() {
        return aspect != null;
    }

    public ClassType getClassType() {
        if (hasAbilityTree()) {
            ClassType atClass = abilityTree().classType();
            if (atClass != null && atClass != ClassType.NONE) {
                return atClass;
            }
        }
        if (hasAspects()) {
            ClassType aspectClass = aspect().classType();
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
        return hasAbilityTree() ? abilityTree().getMainArchetype() : "";
    }

    public int getNodeCount() {
        return hasAbilityTree() ? abilityTree().getNodeCount() : 0;
    }

    public int getAspectCount() {
        return hasAspects() ? aspect().getAspectCount() : 0;
    }

    public int getMaxLevel() {
        int spLevel = hasSkillPoints() ? skillPoints().getMinimumCombatLevel() : 0;
        int atLevel = hasAbilityTree() ? abilityTree().getLevel() : 0;
        int asLevel = hasAspects() ? aspect().getLevel() : 0;

        return Math.max(1, Math.max(spLevel, Math.max(atLevel, asLevel)));
    }

    public MenuCategory getMenuCategory() {
        return switch (type) {
            case LoadoutType.BUILD -> MenuCategory.BUILD_LOADOUT;
            case LoadoutType.ABILITY_TREE -> MenuCategory.ABILITY_TREE_LOADOUT;
            case LoadoutType.SKILL_POINT -> MenuCategory.SKILL_POINT_LOADOUT;
            case LoadoutType.ASPECT -> MenuCategory.ASPECT_LOADOUT;
        };
    }

    // The names are from Static-storage/Reference/abilities.json
    // It's the "archetype" field.
    private Texture getArchetypeTexture() {
        String archetype = getMainArchetype();
        if (archetype.isEmpty()) return null;

        return switch (archetype) {
            // archer
            case "Boltslinger" -> Texture.ARCHETYPE_BOLTSLINGER;
            case "Sharpshooter" -> Texture.ARCHETYPE_SHARPSHOOTER;
            case "Trapper" -> Texture.ARCHETYPE_TRAPPER;

            // assassin
            case "Acrobat" -> Texture.ARCHETYPE_ACROBAT;
            case "Shadestepper" -> Texture.ARCHETYPE_SHADESTEPPER;
            case "Trickster" -> Texture.ARCHETYPE_TRICKSTER;

            // mage
            case "Arcanist" -> Texture.ARCHETYPE_ARCANIST;
            case "Light Bender" -> Texture.ARCHETYPE_LIGHTBENDER;
            case "Riftwalker" -> Texture.ARCHETYPE_RIFTWALKER;

            // shaman
            case "Acolyte" -> Texture.ARCHETYPE_ACOLYTE;
            case "Ritualist" -> Texture.ARCHETYPE_RITUALIST;
            case "Summoner" -> Texture.ARCHETYPE_SUMMONER;

            // warrior
            case "Battle Monk" -> Texture.ARCHETYPE_BATTLE_MONK;
            case "Fallen" -> Texture.ARCHETYPE_FALLEN;
            case "Paladin" -> Texture.ARCHETYPE_PALADIN;

            default -> null;
        };
    }

    private Texture getAspectTexture() {
        if (type != LoadoutType.ASPECT) return null;

        return switch (getClassType()) {
            case ClassType.ARCHER-> Texture.ASPECT_ARCHER;
            case ClassType.ASSASSIN -> Texture.ASPECT_ASSASSIN;
            case ClassType.MAGE -> Texture.ASPECT_MAGE;
            case ClassType.SHAMAN -> Texture.ASPECT_SHAMAN;
            case ClassType.WARRIOR -> Texture.ASPECT_WARRIOR;
            default -> null;
        };
    }

    public Texture getDisplayTexture() {
        Texture archetypeTexture = getArchetypeTexture();
        if (archetypeTexture != null) return archetypeTexture;

        Texture aspectTexture = getAspectTexture();
        if (aspectTexture != null) return aspectTexture;

        return null;
    }

    public Texture getFlameTexture() {
        if (type != LoadoutType.ASPECT) return null;

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

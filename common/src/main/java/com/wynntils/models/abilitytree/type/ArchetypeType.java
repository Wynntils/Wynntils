/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

// The names are from Static-storage/Reference/abilities.json
// It's the "archetypeInfo" field in the nodes.
public enum ArchetypeType {
    ARCHETYPE_BOLTSLINGER("abilityTree.archetypeBoltslinger", "Boltslinger", ClassType.ARCHER),
    ARCHETYPE_SHARPSHOOTER("abilityTree.archetypeSharpshooter", "Sharpshooter", ClassType.ARCHER),
    ARCHETYPE_TRAPPER("abilityTree.archetypeTrapper", "Trapper", ClassType.ARCHER),

    ARCHETYPE_ACROBAT("abilityTree.archetypeAcrobat", "Acrobat", ClassType.ASSASSIN),
    ARCHETYPE_SHADESTEPPER("abilityTree.archetypeShadestepper", "Shadestepper", ClassType.ASSASSIN),
    ARCHETYPE_TRICKSTER("abilityTree.archetypeTrickster", "Trickster", ClassType.ASSASSIN),

    ARCHETYPE_ARCANIST("abilityTree.archetypeArcanist", "Arcanist", ClassType.MAGE),
    ARCHETYPE_LIGHTBENDER("abilityTree.archetypeLightbender", "Light Bender", ClassType.MAGE),
    ARCHETYPE_RIFTWALKER("abilityTree.archetypeRiftwalker", "Riftwalker", ClassType.MAGE),

    ARCHETYPE_ACOLYTE("abilityTree.archetypeAcolyte", "Acolyte", ClassType.SHAMAN),
    ARCHETYPE_RITUALIST("abilityTree.archetypeRitualist", "Ritualist", ClassType.SHAMAN),
    ARCHETYPE_SUMMONER("abilityTree.archetypeSummoner", "Summoner", ClassType.SHAMAN),

    ARCHETYPE_BATTLE_MONK("abilityTree.archetypeBattleMonk", "Battle Monk", ClassType.WARRIOR),
    ARCHETYPE_FALLEN("abilityTree.archetypeFallen", "Fallen", ClassType.WARRIOR),
    ARCHETYPE_PALADIN("abilityTree.archetypePaladin", "Paladin", ClassType.WARRIOR);

    private final String key;
    private final String name;
    private final ClassType classType;

    ArchetypeType(String key, String name, ClassType classType) {
        this.key = key;
        this.name = name;
        this.classType = classType;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Optional<Float> getCustomModelData() {
        return Services.CustomModel.getFloat(key);
    }

    public static ArchetypeType fromName(String name) {
        for (ArchetypeType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public ItemStack generateItemStack() {
        ItemStack itemStack = new ItemStack(Items.POTION);

        float customModelData = getCustomModelData().orElse(-1f);

        itemStack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(customModelData), List.of(), List.of(), List.of()));

        itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

        return itemStack;
    }
}

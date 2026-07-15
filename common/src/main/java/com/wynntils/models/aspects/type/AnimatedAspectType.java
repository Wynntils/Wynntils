package com.wynntils.models.aspects.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.Optional;

public enum AnimatedAspectType {
    ASPECT_ARCHER_ANIMATED("abilityTree.aspectArcherAnimated", ClassType.ARCHER),
    ASPECT_ASSASSIN_ANIMATED("abilityTree.aspectAssassinAnimated", ClassType.ASSASSIN),
    ASPECT_MAGE_ANIMATED("abilityTree.aspectMageAnimated", ClassType.MAGE),
    ASPECT_SHAMAN_ANIMATED("abilityTree.aspectShamanAnimated", ClassType.SHAMAN),
    ASPECT_WARRIOR_ANIMATED("abilityTree.aspectWarriorAnimated", ClassType.WARRIOR);

    private final String key;
    private final ClassType classType;

    AnimatedAspectType(String key, ClassType classType) {
        this.key = key;
        this.classType = classType;
    }

    public String getKey() {
        return key;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Optional<Float> getCustomModelData() {
        return Services.CustomModel.getFloat(key);
    }

    public static AnimatedAspectType fromClassType(ClassType classType) {
        for (AnimatedAspectType type : values()) {
            if (type.getClassType() == classType) {
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

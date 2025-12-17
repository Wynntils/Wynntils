/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public abstract class DataComponentGetEvent<T> extends Event {
    private final ItemStack itemStack;
    private final DataComponentType<T> dataComponentType;
    private final T originalValue;
    private T value;

    protected DataComponentGetEvent(ItemStack itemStack, DataComponentType<T> dataComponentType, T originalValue) {
        this.itemStack = itemStack;
        this.dataComponentType = dataComponentType;
        this.originalValue = originalValue;
        this.value = originalValue;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public DataComponentType<T> getDataComponentType() {
        return dataComponentType;
    }

    public T getOriginalValue() {
        return originalValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        this.value = newValue;
    }

    public static final class CustomModelData
            extends DataComponentGetEvent<net.minecraft.world.item.component.CustomModelData> {
        public CustomModelData(ItemStack itemStack, net.minecraft.world.item.component.CustomModelData original) {
            super(itemStack, DataComponents.CUSTOM_MODEL_DATA, original);
        }
    }

    public static final class DyedItemColor
            extends DataComponentGetEvent<net.minecraft.world.item.component.DyedItemColor> {
        public DyedItemColor(ItemStack itemStack, net.minecraft.world.item.component.DyedItemColor original) {
            super(itemStack, DataComponents.DYED_COLOR, original);
        }
    }

    public static final class EnchantmentGlintOverride extends DataComponentGetEvent<Boolean> {
        public EnchantmentGlintOverride(ItemStack itemStack, Boolean original) {
            super(itemStack, DataComponents.ENCHANTMENT_GLINT_OVERRIDE, original);
        }
    }

    public static final class PotionContents
            extends DataComponentGetEvent<net.minecraft.world.item.alchemy.PotionContents> {
        public PotionContents(ItemStack itemStack, net.minecraft.world.item.alchemy.PotionContents original) {
            super(itemStack, DataComponents.POTION_CONTENTS, original);
        }
    }
}

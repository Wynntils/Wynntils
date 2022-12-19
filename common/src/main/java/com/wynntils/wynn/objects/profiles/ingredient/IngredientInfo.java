/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.ingredient;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IngredientInfo {
    @SerializedName("name")
    private final String materialName;

    @SerializedName("damage")
    private final String metadata;

    public IngredientInfo(String materialName, String metadata) {
        this.materialName = materialName;
        this.metadata = metadata;
    }

    public ItemStack asItemStack() {
        if (materialName == null) {
            return new ItemStack(Items.AIR);
        }

        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(materialName));
        ItemStack itemStack = item.map(ItemStack::new).orElseGet(() -> new ItemStack(Items.AIR));

        if (metadata != null) {
            itemStack.setDamageValue(Integer.parseInt(metadata));
        }

        return itemStack;
    }
}

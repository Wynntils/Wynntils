/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.utils.mc.SkinUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.Unbreakable;

public record ItemMaterial(ItemStack itemStack) {
    public static ItemMaterial getDefaultTomeItemMaterial() {
        ItemStack itemStack = createItemStack(Items.ENCHANTED_BOOK, 0);
        return new ItemMaterial(itemStack);
    }

    public static ItemMaterial getDefaultCharmItemMaterial() {
        // All charms are different items, this is as good as any other item
        ItemStack itemStack = createItemStack(Items.CLAY, 0);
        return new ItemMaterial(itemStack);
    }

    public static ItemMaterial fromPlayerHeadUUID(String uuid) {
        ItemStack itemStack = createItemStack(Items.PLAYER_HEAD, 0);
        SkinUtils.setPlayerHeadFromUUID(itemStack, uuid);

        return new ItemMaterial(itemStack);
    }

    public static ItemMaterial fromGearType(GearType gearType) {
        // Material is missing, so just give generic icon for this type of gear (weapon or accessory)
        ItemStack itemStack = createItemStack(gearType.getDefaultItem(), gearType.getDefaultModel());

        return new ItemMaterial(itemStack);
    }

    public static ItemMaterial fromItemId(String itemId, int customModelData) {
        ItemStack itemStack = createItemStack(getItem(itemId), customModelData);
        return new ItemMaterial(itemStack);
    }

    public static ItemMaterial fromItemTypeCode(int itemTypeCode, int damageCode) {
        String itemId;

        Optional<String> materialNameOverrideOpt = Models.WynnItem.getMaterialName(itemTypeCode, damageCode);
        if (materialNameOverrideOpt.isPresent()) {
            // The vanilla lookup fails for a handful of items, so we have a correctional data set
            itemId = "minecraft:" + materialNameOverrideOpt.get();
        } else {
            // Use normal vanilla lookup
            String toIdString = ItemIdFix.getItem(itemTypeCode);
            String alternativeName = ItemStackTheFlatteningFix.updateItem(toIdString, damageCode);
            itemId = alternativeName != null ? alternativeName : toIdString;
        }

        return fromItemId(itemId, damageCode);
    }

    private static ItemStack createItemStack(Item item, float modelValue) {
        ItemStack itemStack = new ItemStack(item);

        CustomModelData customModelData = new CustomModelData(List.of(modelValue), List.of(), List.of(), List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);
        itemStack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        return itemStack;
    }

    private static Item getItem(String itemId) {
        return BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(itemId));
    }
}

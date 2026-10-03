/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.utils.mc.SkinUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public record ItemMaterial(ItemStackTemplate itemStackTemplate) {
    public ItemStack itemStack() {
        return itemStackTemplate.create();
    }

    public static ItemMaterial getDefaultTomeItemMaterial() {
        ItemStackTemplate template = createTemplate(Items.ENCHANTED_BOOK, 0);
        return new ItemMaterial(template);
    }

    public static ItemMaterial getDefaultCharmItemMaterial() {
        // All charms are different items, this is as good as any other item
        ItemStackTemplate template = createTemplate(Items.CLAY, 0);
        return new ItemMaterial(template);
    }

    public static ItemMaterial fromPlayerHeadUUID(String uuid) {
        ItemStackTemplate template = new ItemStackTemplate(
                BuiltInRegistries.ITEM.wrapAsHolder(Items.PLAYER_HEAD), 1, SkinUtils.createPlayerHeadFromUUID(uuid));

        return new ItemMaterial(template);
    }

    public static ItemMaterial fromGearType(GearType gearType) {
        // Material is missing, so just give generic icon for this type of gear (weapon or accessory)
        ItemStackTemplate template = createTemplate(gearType.getDefaultItem(), gearType.getDefaultModel());

        return new ItemMaterial(template);
    }

    public static ItemMaterial fromItemId(String itemId, int customModelData) {
        ItemStackTemplate template = createTemplate(getItem(itemId), customModelData);
        return new ItemMaterial(template);
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

        Item item = getItem(itemId);

        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponents.DAMAGE, damageCode)
                .set(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .build();

        return new ItemMaterial(new ItemStackTemplate(BuiltInRegistries.ITEM.wrapAsHolder(item), 1, patch));
    }

    private static ItemStackTemplate createTemplate(Item item, float modelValue) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(
                        DataComponents.CUSTOM_MODEL_DATA,
                        new CustomModelData(List.of(modelValue), List.of(), List.of(), List.of()))
                .set(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .build();

        return new ItemStackTemplate(BuiltInRegistries.ITEM.wrapAsHolder(item), 1, patch);
    }

    private static Item getItem(String itemId) {
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null) {
            WynntilsMod.warn("Invalid item id in API: " + itemId + ".");
            return Items.BARRIER;
        }

        Item item = BuiltInRegistries.ITEM.getValue(identifier);
        if (item == Items.AIR) {
            WynntilsMod.warn("Unknown/empty item id in API: " + itemId + ".");
            return Items.BARRIER;
        }

        return item;
    }
}

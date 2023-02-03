/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record GearMaterial(ItemStack itemStack) {
    public static GearMaterial fromArmorType(String materialType, GearType gearType, CustomColor color) {
        String itemId = (materialType.equals("chain") ? "chainmail" : materialType) + "_"
                + gearType.name().toLowerCase(Locale.ROOT);

        ItemStack itemStack = createItemStack(getItem("minecraft:" + itemId), 0);
        if (color != null) {
            // color is only set in case of leather
            CompoundTag tag = itemStack.getOrCreateTag();
            CompoundTag displayTag = new CompoundTag();
            tag.put("display", displayTag);
            displayTag.putInt("color", color.asInt());
            itemStack.setTag(tag);
        }

        return new GearMaterial(itemStack);
    }

    public static GearMaterial fromGearType(GearType gearType) {
        // Material is missing, so just give generic icon for this type of gear (weapon or accessory)
        ItemStack itemStack = createItemStack(gearType.getDefaultItem(), gearType.getDefaultDamage());

        return new GearMaterial(itemStack);
    }

    public static GearMaterial fromItemTypeCode(int itemTypeCode, int damageCode) {
        String toIdString = ItemIdFix.getItem(itemTypeCode);
        String alternativeName = ItemStackTheFlatteningFix.updateItem(toIdString, damageCode);
        String itemId = alternativeName != null ? alternativeName : toIdString;

        // FIXME: The vanilla lookup still fails for e.g. Totem of Undying.
        // In this case, AIR is returned.
        ItemStack itemStack = createItemStack(getItem(itemId), damageCode);

        return new GearMaterial(itemStack);
    }

    private static ItemStack createItemStack(Item item, int damageValue) {
        ItemStack itemStack = new ItemStack(item);

        itemStack.setDamageValue(damageValue);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        return itemStack;
    }

    private static Item getItem(String itemId) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item == null) {
            WynntilsMod.error("Cannot create item for " + itemId);
            throw new RuntimeException();
        }
        return item;
    }
}

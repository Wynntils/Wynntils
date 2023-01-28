/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GearMaterial {
    // FIXME: Somehow get this together so we can present a suitable item icon...

    public GearMaterial(String armorType, GearType gearType, CustomColor color) {
        // armorType is any of: CHAIN DIAMOND GOLDEN IRON LEATHER
        // color is only set in case of LEATHER
    }

    public GearMaterial(GearType gearType) {
        // Material is missing, so just give generic icon for this type of gear (weapon or accessory)
    }

    public GearMaterial(String itemId, int damageCode) {
        // itemId is e.g. "stick", not "minecraft:stick"
    }

    public ItemStack asItemStack() {
        return new ItemStack(Items.BEDROCK);

        // FIXME: Need to work with GearMaterial!
        /*
               if (materialName == null) {
                   ItemStack stack = new ItemStack(type.getDefaultItem());
                   stack.setDamageValue(type.getDefaultDamage());

                   return stack;
               }

               Optional<Item> item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(materialName));
               ItemStack itemStack = item.map(ItemStack::new).orElseGet(() -> new ItemStack(Items.AIR));

               if (metadata != null) {
                   itemStack.setDamageValue(Integer.parseInt(metadata));
               }

               return itemStack;

        */
    }

    public boolean hasColorCode() {
        // FIXME
        return false;
    }

    public int getColorCode() {
        // FIXME
        return 0;
    }
}

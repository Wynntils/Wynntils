/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GearItem;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemUtils {
    public static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");

    public static boolean isWeapon(ItemStack itemStack) {
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isEmpty()) return false;

        return gearItemOpt.get().getGearType().isWeapon();
    }

    public static MutableComponent getNonGearDescription(ItemStack itemStack, String gearName) {
        if (gearName.contains("Crafted")) {
            return Component.literal(gearName).withStyle(ChatFormatting.DARK_AQUA);
        }

        // this solves an unidentified item showcase exploit
        // boxes items are STONE_SHOVEL, 1 represents UNIQUE boxes and 6 MYTHIC boxes
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            return Component.literal("Unidentified Item")
                    .withStyle(
                            GearTier.fromBoxDamage(itemStack.getDamageValue()).getChatFormatting());
        }
        return null;
    }
}

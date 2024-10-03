/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemUtils {
    private static final String EMPTY_ACCESSORY_SLOT = "§7Accessory Slot";
    public static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");

    public static boolean isWeapon(ItemStack itemStack) {
        Optional<GearTypeItemProperty> gearItemOpt =
                Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return false;

        return gearItemOpt.get().getGearType().isWeapon();
    }

    public static boolean isGatheringTool(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        return wynnItemOpt
                .filter(wynnItem -> wynnItem instanceof GatheringToolItem)
                .isPresent();
    }

    public static boolean isEmptyAccessorySlot(ItemStack itemStack) {
        return itemStack.getHoverName().getString().equals(EMPTY_ACCESSORY_SLOT);
    }

    public static StyledText getItemName(ItemStack itemStack) {
        return StyledText.fromComponent(itemStack.getHoverName());
    }

    public static boolean isItemListsEqual(List<ItemStack> firstItems, List<ItemStack> secondItems) {
        if (firstItems.size() != secondItems.size()) return false;

        for (int i = 0; i < firstItems.size(); i++) {
            ItemStack newItem = firstItems.get(i);
            ItemStack oldItem = secondItems.get(i);
            if (!isItemEqual(oldItem, newItem)) return false;
        }
        return true;
    }

    public static boolean isItemEqual(ItemStack oldItem, ItemStack newItem) {
        if (oldItem == null || newItem == null) return oldItem != newItem;

        return newItem.getItem().equals(oldItem.getItem())
                && newItem.getDamageValue() == oldItem.getDamageValue()
                && newItem.getCount() == oldItem.getCount()
                && ItemStack.isSameItemSameComponents(oldItem, newItem);
    }

    public static boolean areItemsSimilar(ItemStack a, ItemStack b) {
        if (a.isEmpty()) {
            return b.isEmpty();
        } else {
            return !b.isEmpty()
                    && a.getHoverName().getString().equals(b.getHoverName().getString());
        }
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

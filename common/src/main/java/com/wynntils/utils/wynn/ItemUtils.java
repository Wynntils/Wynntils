/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
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
    public static final Pattern REQUIRED_CLASS_PATTERN = Pattern.compile("^✔ Class Req: (.+)$");
    private static final Pattern REQUIRED_LEVEL_PATTERN = Pattern.compile("^✔ Combat Lv. Min: ([0-9]+)$");

    public static boolean isWeapon(ItemStack itemStack) {
        Optional<GearTypeItemProperty> gearItemOpt =
                Models.Item.asWynnItemPropery(itemStack, GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return false;

        return gearItemOpt.get().getGearType().isWeapon();
    }

    public static StyledText getItemName(ItemStack itemStack) {
        return StyledText.fromComponent(itemStack.getHoverName());
    }

    public static boolean isItemListsEqual(List<ItemStack> firstItems, List<ItemStack> secondItems) {
        if (firstItems.size() != secondItems.size()) return false;

        for (int i = 0; i < firstItems.size(); i++) {
            ItemStack newItem = firstItems.get(i);
            ItemStack oldItem = secondItems.get(i);
            if (!newItem.getItem().equals(oldItem.getItem())
                    || newItem.getDamageValue() != oldItem.getDamageValue()
                    || newItem.getCount() != oldItem.getCount()
                    || !ItemStack.tagMatches(oldItem, newItem)) {
                return false;
            }
        }
        return true;
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

    public static boolean canBeWielded(ItemStack itemStack) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        return isWeapon(itemStack)
                && lore.stream()
                        .anyMatch(styledText -> styledText.matches(REQUIRED_CLASS_PATTERN, PartStyle.StyleType.NONE))
                && lore.stream()
                        .anyMatch(styledText -> styledText.matches(REQUIRED_LEVEL_PATTERN, PartStyle.StyleType.NONE));
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.items.game.HorseItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HorseAnnotator implements ItemAnnotator {
    private static final Pattern HORSE_PATTERN = Pattern.compile(
            "§7Tier (\\d)§6Speed: (\\d+)/(\\d+)§6Jump: \\d+/\\d+§5Armour: None§bXp: (\\d+)/100(?:§cUntradable Item)?(:?§7Name: (.+))?");

    private static boolean isHorse(ItemStack itemStack) {
        return itemStack.getItem() == Items.SADDLE
                && itemStack.getHoverName().getString().contains("Horse");
    }

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        if (!isHorse(itemStack)) return null;

        String lore = ItemUtils.getStringLore(itemStack);
        Matcher m = HORSE_PATTERN.matcher(lore);

        if (!m.matches()) return null;

        int tier = Integer.parseInt(m.group(1));
        int level = Integer.parseInt(m.group(2));
        int maxLevel = Integer.parseInt(m.group(3));
        int xp = Integer.parseInt(m.group(4));
        String name = m.group(5);

        return new HorseItem(tier, new CappedValue(level, maxLevel), xp, name);
    }
}

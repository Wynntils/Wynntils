/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftedGearItemStack extends WynnItemStack implements HighlightedItem, HotbarHighlightedItem {

    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");

    private int currentDurability;
    private int maxDurability;

    public CraftedGearItemStack(ItemStack stack) {
        super(stack);

        // parse durability
        List<Component> lore = getOriginalTooltip();
        for (Component line : lore) {
            String unformatted = WynnUtils.normalizeBadString(line.getString());

            Matcher durabilityMatcher = DURABILITY_PATTERN.matcher(unformatted);
            if (!durabilityMatcher.find()) continue;

            this.currentDurability = Integer.parseInt(durabilityMatcher.group(1));
            this.maxDurability = Integer.parseInt(durabilityMatcher.group(2));
            break;
        }
    }

    public float getDurabilityPct() {
        return (currentDurability * 1f) / maxDurability;
    }

    @Override
    public CustomColor getHighlightColor(Screen screen, Slot slot) {
        return ItemTier.CRAFTED.getHighlightColor();
    }

    @Override
    public CustomColor getHotbarColor() {
        return ItemTier.CRAFTED.getHighlightColor();
    }
}

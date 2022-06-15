/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DurabilityProperty extends ItemProperty {
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");

    private int currentDurability;
    private int maxDurability;

    public DurabilityProperty(WynnItemStack stack) {
        super(stack);

        // parse durability
        List<Component> lore = stack.getOriginalTooltip();
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

    public static boolean hasDurability(ItemStack stack) {
        for (Component c : ItemUtils.getTooltipLines(stack)) {
            if (c.getString().endsWith(" Durability]")) return true;
        }
        return false;
    }
}

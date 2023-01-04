/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.utils.WynnItemMatchers;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;

/** Represents the durability on items with that value (crafted items, gathering tools) */
public class DurabilityProperty extends ItemProperty {
    private int currentDurability;
    private int maxDurability;
    private float percent;

    public DurabilityProperty(WynnItemStack item) {
        super(item);

        // parse durability
        List<Component> lore = item.getOriginalTooltip();
        for (Component line : lore) {
            Matcher durabilityMatcher = WynnItemMatchers.durabilityLineMatcher(line);
            if (!durabilityMatcher.find()) continue;

            this.currentDurability = Integer.parseInt(durabilityMatcher.group(1));
            this.maxDurability = Integer.parseInt(durabilityMatcher.group(2));
            this.percent = (currentDurability * 1f) / maxDurability;
            break;
        }
    }

    public float getDurabilityPercent() {
        return percent;
    }
}

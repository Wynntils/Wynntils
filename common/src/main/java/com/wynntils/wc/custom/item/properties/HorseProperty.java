/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.custom.item.WynnItemStack;

import java.util.List;

public class HorseProperty extends ItemProperty {

    private int xp;
    private int level;
    private int maxLevel;
    private int tier;

    public HorseProperty(WynnItemStack item) {
        super(item);

        // Parse xp, level, max level, and tier
        List<String> lore = ItemUtils.getLore(item);

        tier = Integer.parseInt(lore.get(0).substring(7));
        level = Integer.parseInt(lore.get(1).substring(9, lore.get(1).indexOf('/')));
        maxLevel = Integer.parseInt(lore.get(1).substring(lore.get(1).indexOf('/')+1));
        xp = Integer.parseInt(lore.get(4).substring(6, lore.get(4).indexOf('/')));
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getTier() {
        return tier;
    }

}

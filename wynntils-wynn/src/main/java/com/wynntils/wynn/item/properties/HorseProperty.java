/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.item.WynnItemStack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorseProperty extends ItemProperty {

    private final Pattern HORSE_PATTERN = Pattern.compile(
            "§7Tier (\\d)§6Speed: (\\d+)/(\\d+)§6Jump: \\d+/\\d+§5Armour: None§bXp: (\\d+)/100(?:§cUntradable Item)?(:?§7Name: (.+))?");
    private int xp = -1;
    private int level = -1;
    private int maxLevel = -1;
    private int tier = -1;
    private String name = "";

    public HorseProperty(WynnItemStack item) {
        super(item);

        // Parse xp, level, max level, and tier
        String lore = ItemUtils.getStringLore(item);
        Matcher m = HORSE_PATTERN.matcher(lore);

        if (m.matches()) {
            tier = Integer.parseInt(m.group(1));
            level = Integer.parseInt(m.group(2));
            maxLevel = Integer.parseInt(m.group(3));
            xp = Integer.parseInt(m.group(4));
            name = m.group(5);
        }
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

    public String getName() {
        return name;
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemInfoContainer {
    private static final Pattern COLOR_PATTERN =
            Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    String material;
    ItemType type;
    String set;
    ItemDropType dropType;
    String armorColor = null;

    public ItemInfoContainer(
            String material, ItemType type, String set, ItemDropType dropType, String armorColor) {}

    public ItemDropType getDropType() {
        return dropType;
    }

    public ItemType getType() {
        return type;
    }

    public String getArmorColor() {
        return armorColor;
    }

    public String getSet() {
        return set;
    }

    public String getMaterial() {
        return material;
    }

    public boolean isArmorColorValid() {
        return armorColor != null && COLOR_PATTERN.matcher(armorColor).find();
    }

    public int getArmorColorAsInt() {
        if (armorColor == null) return 0;

        Matcher m = COLOR_PATTERN.matcher(getArmorColor());
        if (!m.find()) return 0;

        int r = Integer.parseInt(m.group(1));
        int g = Integer.parseInt(m.group(2));
        int b = Integer.parseInt(m.group(3));

        return (r << 16) + (g << 8) + b;
    }
}

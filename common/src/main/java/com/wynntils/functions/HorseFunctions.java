/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.mc.utils.InventoryUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.ItemProperty;
import net.minecraft.world.item.ItemStack;

public class HorseFunctions {

    private static WynnItemStack getHorse() {
        if (InventoryUtils.findHorseSlotNum() == -1) return null;
        ItemStack is = McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum());
        if (!(is instanceof WynnItemStack horse)) return null;
        return horse;
    }

    public static class HorseLevelFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (getHorse() == null) return "??";
            return String.valueOf(getHorse().getProperty(ItemProperty.HORSE).getLevel());
        }
    }

    public static class HorseLevelMaxFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (getHorse() == null) return "??";
            return String.valueOf(getHorse().getProperty(ItemProperty.HORSE).getMaxLevel());
        }
    }

    public static class HorseXpFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (getHorse() == null) return null;
            return String.valueOf(getHorse().getProperty(ItemProperty.HORSE).getXp());
        }
    }

    public static class HorseTierFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (getHorse() == null) return null;
            return String.valueOf(getHorse().getProperty(ItemProperty.HORSE).getTier());
        }
    }
}

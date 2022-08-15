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

    public static class horseLevelFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            ItemStack is = McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum());
            if (!(is instanceof WynnItemStack horse)) return "??";

            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getLevel());
        }
    }

    public static class horseLevelMaxFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            ItemStack is = McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum());
            if (!(is instanceof WynnItemStack horse)) return "??";

            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getMaxLevel());
        }
    }

    public static class horseXpFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            ItemStack is = McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum());
            if (!(is instanceof WynnItemStack horse)) return "??";

            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getXp());
        }
    }

    public static class horseTierFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "?";

            ItemStack is = McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum());
            if (!(is instanceof WynnItemStack horse)) return "??";

            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getTier());
        }
    }
}

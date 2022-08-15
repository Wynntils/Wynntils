/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import com.wynntils.mc.utils.InventoryUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.HorseProperty;

public class HorseFunctions {

    public static class levelFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            WynnItemStack horse = new WynnItemStack(McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum()));
            return String.valueOf(horse.getProperty(HorseProperty.class).getLevel());
        }
    }

    public static class maxLevelFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            WynnItemStack horse = new WynnItemStack(McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum()));
            return String.valueOf(horse.getProperty(HorseProperty.class).getMaxLevel());
        }
    }

    public static class xpFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "??";

            WynnItemStack horse = new WynnItemStack(McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum()));
            return String.valueOf(horse.getProperty(HorseProperty.class).getXp());
        }
    }

    public static class tierFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            if (InventoryUtils.findHorseSlotNum() == -1) return "?";
            WynnItemStack horse = new WynnItemStack(McUtils.player().getInventory().getItem(InventoryUtils.findHorseSlotNum()));
            return String.valueOf(horse.getProperty(HorseProperty.class).getTier());
        }
    }
}
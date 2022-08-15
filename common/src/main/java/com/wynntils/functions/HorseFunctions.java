/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.utils.InventoryUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.custom.item.ItemStackTransformModel;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.ItemProperty;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class HorseFunctions {

    private static WynnItemStack getHorse() {
        int horseSlot = InventoryUtils.findHorseSlotNum();
        if (horseSlot == -1) return null;
        ItemStack is = McUtils.player().getInventory().getItem(horseSlot);
        if (!(is instanceof WynnItemStack horse)) return null;
        return horse;
    }

    public static class HorseLevelFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return "??";
            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getLevel());
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }
    }

    public static class HorseLevelMaxFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return "??";
            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getMaxLevel());
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }
    }

    public static class HorseXpFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getXp());
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }
    }

    public static class HorseTierFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return String.valueOf(horse.getProperty(ItemProperty.HORSE).getTier());
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }
    }

    public static class HorseNameFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return horse.getProperty(ItemProperty.HORSE).getName();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }
    }
}

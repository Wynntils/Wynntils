/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.ItemProperty;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class HorseFunctions {

    private static WynnItemStack getHorse() {
        int horseSlot = InventoryUtils.findHorseSlotNum();
        if (horseSlot == -1) return null;
        ItemStack is = McUtils.inventory().getItem(horseSlot);
        if (!(is instanceof WynnItemStack horse)) return null;
        return horse;
    }

    public static class HorseLevelFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return horse.getProperty(ItemProperty.HORSE).getLevel();
        }

        @Override
        public List<? extends Model> getModelDependencies() {
            return List.of(Models.HorseProperty);
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_lvl");
        }
    }

    public static class HorseLevelMaxFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return horse.getProperty(ItemProperty.HORSE).getMaxLevel();
        }

        @Override
        public List<? extends Model> getModelDependencies() {
            return List.of(Models.HorseProperty);
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_mlvl");
        }
    }

    public static class HorseXpFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return horse.getProperty(ItemProperty.HORSE).getXp();
        }

        @Override
        public List<? extends Model> getModelDependencies() {
            return List.of(Models.HorseProperty);
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_xp");
        }
    }

    public static class HorseTierFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            return horse.getProperty(ItemProperty.HORSE).getTier();
        }

        @Override
        public List<? extends Model> getModelDependencies() {
            return List.of(Models.HorseProperty);
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_tier");
        }
    }

    public static class HorseNameFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            WynnItemStack horse = getHorse();
            if (horse == null) return null;
            String name = horse.getProperty(ItemProperty.HORSE).getName();
            return (name.isEmpty()) ? null : name;
        }

        @Override
        public List<? extends Model> getModelDependencies() {
            return List.of(Models.HorseProperty);
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_name");
        }
    }
}

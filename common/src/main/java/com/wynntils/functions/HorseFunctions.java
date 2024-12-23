/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class HorseFunctions {
    public static class CappedHorseLevelFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return CappedValue.EMPTY;

            return horse.get().getLevel();
        }
    }

    public static class CappedHorseXpFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return CappedValue.EMPTY;

            return horse.get().getXp();
        }
    }

    public static class CappedHorseTotalLevelTimeFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            Optional<CappedValue> result = Models.Horse.calculateNextLevelCumulativeSeconds();

            return result.isPresent() ? result.get() : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_tot_lvl_time");
        }
    }

    public static class HorseLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return -1;

            return horse.get().getLevel().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_lvl");
        }
    }

    public static class HorseLevelMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return -1;

            return horse.get().getLevel().max();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_mlvl");
        }
    }

    public static class HorseXpFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return -1;

            return horse.get().getXp().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_xp");
        }
    }

    public static class HorseTierFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return -1;

            return horse.get().getTier().getNumeral();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_tier");
        }
    }

    public static class HorseNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return "";

            Optional<String> name = horse.get().getName();
            return name.isPresent() ? name.get() : "";
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_name");
        }
    }

    public static class HorseLevelTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<Integer> result = Models.Horse.calculateNextLevelSeconds();

            return result.isPresent() ? result.get() : -1;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_lvl_time");
        }
    }
}

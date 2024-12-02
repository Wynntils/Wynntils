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

    // Approximate time (in minutes) until next horse level
    public static class CappedHorseLevelTimeFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return CappedValue.EMPTY;
            if (horse.get().getLevel().current() == horse.get().getLevel().max()) return CappedValue.EMPTY;

            // This is based off of a formula from https://wynncraft.fandom.com/wiki/Horses#Levels
            double levelProgress = 3.0 * horse.get().getLevel().current() + 2;
            double xpProgress = 100.0 - horse.get().getXp().current();

            double result = levelProgress / 6.0 * (xpProgress / 100.0) * 100.0;
            double resultMax = levelProgress / 6.0 * 100.0;

            return new CappedValue((int) Math.ceil(result), (int) Math.ceil(resultMax));
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

    // Approximate time (in minutes) until next horse level
    public static class HorseLevelTimeFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Optional<HorseItem> horse = Models.Horse.getHorse();
            if (horse.isEmpty()) return -1.0;
            if (horse.get().getLevel().current() == horse.get().getLevel().max()) return -1.0;

            // This is based off of a formula from https://wynncraft.fandom.com/wiki/Horses#Levels
            double levelProgress = 3.0 * horse.get().getLevel().current() + 2;
            double xpProgress = 100.0 - horse.get().getXp().current();

            double result = levelProgress / 6.0 * (xpProgress / 100.0) * 100.0;

            return Math.ceil(result) / 100.0;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("h_lvl_time");
        }
    }
}

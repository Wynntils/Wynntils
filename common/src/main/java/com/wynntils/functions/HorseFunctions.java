/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.models.items.items.game.HorseItem;
import java.util.List;

public class HorseFunctions {
    public static class HorseLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            HorseItem horse = Models.Horse.getHorse();
            if (horse == null) return null;
            return horse.getLevel().current();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_lvl");
        }
    }

    public static class HorseLevelMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            HorseItem horse = Models.Horse.getHorse();
            if (horse == null) return null;
            return horse.getLevel().max();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_mlvl");
        }
    }

    public static class HorseXpFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            HorseItem horse = Models.Horse.getHorse();
            if (horse == null) return null;
            return horse.getXp();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_xp");
        }
    }

    public static class HorseTierFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            HorseItem horse = Models.Horse.getHorse();
            if (horse == null) return null;
            return horse.getTier();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_tier");
        }
    }

    public static class HorseNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            HorseItem horse = Models.Horse.getHorse();
            if (horse == null) return null;
            String name = horse.getName();
            return (name.isEmpty()) ? null : name;
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_name");
        }
    }
}

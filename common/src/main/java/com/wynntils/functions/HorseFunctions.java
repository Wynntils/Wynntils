/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Managers;
import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.wynn.handleditems.items.game.HorseItem;
import java.util.List;

public class HorseFunctions {

    public static class HorseLevelFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            HorseItem horse = Managers.Horse.getHorse();
            if (horse == null) return null;
            return horse.getLevel().getCurrent();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_lvl");
        }
    }

    public static class HorseLevelMaxFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            HorseItem horse = Managers.Horse.getHorse();
            if (horse == null) return null;
            return horse.getLevel().getMax();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_mlvl");
        }
    }

    public static class HorseXpFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            HorseItem horse = Managers.Horse.getHorse();
            if (horse == null) return null;
            return horse.getXp();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_xp");
        }
    }

    public static class HorseTierFunction extends ActiveFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            HorseItem horse = Managers.Horse.getHorse();
            if (horse == null) return null;
            return horse.getTier();
        }

        @Override
        public List<String> getAliases() {
            return List.of("h_tier");
        }
    }

    public static class HorseNameFunction extends ActiveFunction<String> {
        @Override
        public String getValue(String argument) {
            HorseItem horse = Managers.Horse.getHorse();
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

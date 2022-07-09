/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.google.common.collect.Lists;
import com.wynntils.wc.ModelLoader;
import com.wynntils.wc.model.Character;
import java.util.List;

public class WynnUtils {
    /**
     * Removes the characters 'À' ('\u00c0') and '\u058e' that is sometimes added in Wynn APIs and
     * replaces '\u2019' (RIGHT SINGLE QUOTATION MARK) with '\'' (And trims)
     *
     * @param input string
     * @return the string without these two chars
     */
    public static String normalizeBadString(String input) {
        if (input == null) return "";
        return input.trim()
                .replace("ÀÀÀ", " ")
                .replace("À", "")
                .replace("\u058e", "")
                .replace('\u2019', '\'')
                .trim();
    }

    public static boolean onServer() {
        return ModelLoader.getWorldState().onServer();
    }

    public static boolean onWorld() {
        return ModelLoader.getWorldState().onWorld();
    }

    public static Character getCharacter() {
        return ModelLoader.getCharacter();
    }

    public static List<String> getWynnServerTypes() {
        return Lists.newArrayList("WC", "lobby", "GM", "DEV", "WAR", "HB");
    }
}

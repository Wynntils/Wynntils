/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.WorldStateManager;

public final class WynnUtils {
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
        return WorldStateManager.onServer();
    }

    public static boolean onWorld() {
        return WorldStateManager.onWorld();
    }

    public static CharacterManager.CharacterInfo getCharacterInfo() {
        return CharacterManager.getCharacterInfo();
    }

    public static boolean hasCharacterInfo() {
        return CharacterManager.hasCharacter();
    }
}

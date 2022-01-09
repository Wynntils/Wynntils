/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * Converts a delimited list into a {@link java.util.List} of strings
     *
     * <p>e.g. "1, 2, 3,, 4," for delimiter "," -> returns a list of "1", "2", "3", "4"
     */
    public static List<String> parseStringToList(String input, String delimiter) {
        List<String> result = new ArrayList<>();

        for (String element : input.split(Pattern.quote(delimiter))) {
            result.add(element.strip());
        }

        return result;
    }

    public static List<String> parseStringToList(String input) {
        return parseStringToList(input, ",");
    }
}

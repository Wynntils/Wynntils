/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.colors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private static final String COLOR_CHAR = "§";
    private static final String HEX_PATTERN = "#([0-9a-fA-F]{6})([0-9a-fA-F]{2})?";

    public static String stripColors(String input) {
        Pattern hexColorPattern = Pattern.compile(COLOR_CHAR + HEX_PATTERN);
        Matcher matcher = hexColorPattern.matcher(input);

        String output = matcher.replaceAll("");

        Pattern colorPattern = Pattern.compile(COLOR_CHAR + "[0-9a-fA-F]");
        matcher = colorPattern.matcher(output);

        return matcher.replaceAll("");
    }
}

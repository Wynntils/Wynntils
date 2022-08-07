/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.mc.utils.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;

public final class StringUtils {
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

    public static String capitalizeFirst(String input) {
        if (input.isEmpty()) return "";
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static String uncapitalizeFirst(String input) {
        if (input.isEmpty()) return "";
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    public static String[] wrapTextBySize(String s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        String[] stringArray = s.split(" ");
        StringBuilder result = new StringBuilder();
        int length = 0;

        for (String string : stringArray) {
            String[] lines = string.split("\\\\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i > 0 || length + font.width(line) >= maxPixels) {
                    result.append('\n');
                    length = 0;
                }
                if (!line.isEmpty()) {
                    result.append(line).append(' ');
                    length += font.width(line) + spaceSize;
                }
            }
        }

        return result.toString().split("\n");
    }

    /**
     * Creates a new pattern, but replaces all occurrences of '§' in the regex with an expression for detecting any color code
     * @param regex - the expression to be modified and compiled
     * @return a Pattern with the modified regex
     */
    public static Pattern compileCCRegex(String regex) {
        return Pattern.compile(regex.replace("§", "(?:§[0-9a-fklmnor])*"));
    }
}

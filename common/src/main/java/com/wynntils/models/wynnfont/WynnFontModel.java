/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnfont;

import com.wynntils.core.components.Model;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WynnFontModel extends Model {
    private static final char NEGATIVE_SPACE = '\uE012';
    private static final char NEGATIVE_SPACE_EDGE = '\u2064';
    private static final char BACKGROUND = '\uE00F';
    private static final Map<Character, Character> normalToFancy = Map.ofEntries(
            Map.entry('a', '\uE040'),
            Map.entry('b', '\uE041'),
            Map.entry('c', '\uE042'),
            Map.entry('d', '\uE043'),
            Map.entry('e', '\uE044'),
            Map.entry('f', '\uE045'),
            Map.entry('g', '\uE046'),
            Map.entry('h', '\uE047'),
            Map.entry('i', '\uE048'),
            Map.entry('j', '\uE049'),
            Map.entry('k', '\uE04A'),
            Map.entry('l', '\uE04B'),
            Map.entry('m', '\uE04C'),
            Map.entry('n', '\uE04D'),
            Map.entry('o', '\uE04E'),
            Map.entry('p', '\uE04F'),
            Map.entry('q', '\uE050'),
            Map.entry('r', '\uE051'),
            Map.entry('s', '\uE052'),
            Map.entry('t', '\uE053'),
            Map.entry('u', '\uE054'),
            Map.entry('v', '\uE055'),
            Map.entry('w', '\uE056'),
            Map.entry('x', '\uE057'),
            Map.entry('y', '\uE058'),
            Map.entry('z', '\uE059'),
            Map.entry('?', '\uE05A'),
            Map.entry('[', '\uE05B'),
            Map.entry(']', '\uE05C'),
            Map.entry('\\', '\uE05D'),
            Map.entry('%', '\uE05E'),
            Map.entry('&', '\uE05F'),
            Map.entry('0', '\uE060'),
            Map.entry('1', '\uE061'),
            Map.entry('2', '\uE062'),
            Map.entry('3', '\uE063'),
            Map.entry('4', '\uE064'),
            Map.entry('5', '\uE065'),
            Map.entry('6', '\uE066'),
            Map.entry('7', '\uE067'),
            Map.entry('8', '\uE068'),
            Map.entry('9', '\uE069'));

    public WynnFontModel() {
        super(List.of());
    }

    public String toBackgroundFont(
            String text, CustomColor textColor, CustomColor backgroundColor, String leftEdge, String rightEdge) {
        StringBuilder sb = new StringBuilder();
        BackgroundEdge left = BackgroundEdge.fromString(leftEdge);
        BackgroundEdge right = BackgroundEdge.fromString(rightEdge);
        if (left != null) {
            sb.append("§");
            sb.append(backgroundColor.toHexString());
            sb.append(left.getLeft());
            sb.append(NEGATIVE_SPACE_EDGE);
        }
        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') {
                sb.append("§");
                sb.append(backgroundColor.toHexString());
                sb.append(BACKGROUND);
                sb.append(NEGATIVE_SPACE);
                sb.append(' ');
                continue;
            }
            Character fancy = normalToFancy.get(c);
            if (fancy == null) continue;
            sb.append("§");
            sb.append(backgroundColor.toHexString());
            sb.append(BACKGROUND);
            sb.append(NEGATIVE_SPACE);
            sb.append("§");
            sb.append(textColor.toHexString());
            sb.append(fancy);
        }
        if (right != null) {
            sb.append("§");
            sb.append(backgroundColor.toHexString());
            sb.append(right.getRight());
        }
        return sb.toString();
    }

    public String toFancyFont(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            Character fancy = normalToFancy.get(c);
            if (fancy == null) continue;
            sb.append(fancy);
        }
        return sb.toString();
    }
}

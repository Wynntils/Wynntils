/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnfont;

import com.wynntils.core.components.Model;
import com.wynntils.utils.colors.CustomColor;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class WynnFontModel extends Model {
    private static final char NEGATIVE_SPACE = '\uE012';
    private static final char NEGATIVE_SPACE_EDGE = '\u2064';
    private static final char BACKGROUND = '\uE00F';
    public static final List<Character> normalCharacters = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '?', '[', ']', '\\', '%', '&', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    public static final List<Character> normalSpecialCharacters = List.of('!', '(', ')', '<', '=', '>');
    private static final List<Character> fancyCharacters = List.of(
            '\uE040', '\uE041', '\uE042', '\uE043', '\uE044', '\uE045', '\uE046', '\uE047', '\uE048', '\uE049',
            '\uE04A', '\uE04B', '\uE04C', '\uE04D', '\uE04E', '\uE04F', '\uE050', '\uE051', '\uE052', '\uE053',
            '\uE054', '\uE055', '\uE056', '\uE057', '\uE058', '\uE059', '\uE05A', '\uE05B', '\uE05C', '\uE05D',
            '\uE05E', '\uE05F', '\uE060', '\uE061', '\uE062', '\uE063', '\uE064', '\uE065', '\uE066', '\uE067',
            '\uE068', '\uE069');
    public static final List<Character> fancySpecialCharacters =
            List.of('\uE06A', '\uE06B', '\uE06C', '\uE06D', '\uE06E', '\uE06F');
    private static final Map<Character, Character> normalToFancy = new HashMap<>();
    private static final Map<Character, Character> normalToBackground = new HashMap<>();

    public WynnFontModel() {
        super(List.of());

        createFontMaps();
    }

    public String toBackgroundFont(
            String text, CustomColor textColor, CustomColor backgroundColor, String leftEdge, String rightEdge) {
        StringBuilder sb = new StringBuilder();
        BackgroundEdge left = BackgroundEdge.fromString(leftEdge);
        BackgroundEdge right = BackgroundEdge.fromString(rightEdge);
        if (left != BackgroundEdge.NONE) {
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
            Character fancy = normalToBackground.get(c);
            if (fancy == null) continue;
            sb.append("§");
            sb.append(backgroundColor.toHexString());
            sb.append(BACKGROUND);
            sb.append(NEGATIVE_SPACE);
            sb.append("§");
            sb.append(textColor.toHexString());
            sb.append(fancy);
        }
        if (right != BackgroundEdge.NONE) {
            sb.append("§");
            sb.append(backgroundColor.toHexString());
            sb.append(NEGATIVE_SPACE_EDGE);
            sb.append(right.getRight());
        }
        return sb.toString();
    }

    public String toFancyFont(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            Character fancy = normalToFancy.get(c);
            if (fancy == null) continue;
            sb.append(fancy);
        }
        return sb.toString();
    }

    private void createFontMaps() {
        for (int i = 0; i < fancyCharacters.size(); i++) {
            normalToFancy.put(normalCharacters.get(i), fancyCharacters.get(i));
            normalToBackground.put(normalCharacters.get(i), fancyCharacters.get(i));
        }
        for (int i = 0; i < fancySpecialCharacters.size(); i++) {
            normalToFancy.put(normalSpecialCharacters.get(i), fancySpecialCharacters.get(i));
        }
    }
}

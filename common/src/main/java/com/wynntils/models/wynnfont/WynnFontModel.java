/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnfont;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.FontLookup;
import com.wynntils.utils.colors.CustomColor;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class WynnFontModel extends Model {
    private static final char NEGATIVE_SPACE = '\uE012';
    private static final char NEGATIVE_SPACE_EDGE = '\u2064';
    private static final char BACKGROUND = '\uE00F';
    private static final List<Character> normalCharacters = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '?', '[', ']', '\\', '%', '&', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private static final List<Character> normalSpecialCharacters = List.of('!', '(', ')', '<', '=', '>');
    private static final List<Character> fancyCharacters = List.of(
            '\uE040', '\uE041', '\uE042', '\uE043', '\uE044', '\uE045', '\uE046', '\uE047', '\uE048', '\uE049',
            '\uE04A', '\uE04B', '\uE04C', '\uE04D', '\uE04E', '\uE04F', '\uE050', '\uE051', '\uE052', '\uE053',
            '\uE054', '\uE055', '\uE056', '\uE057', '\uE058', '\uE059', '\uE05A', '\uE05B', '\uE05C', '\uE05D',
            '\uE05E', '\uE05F', '\uE060', '\uE061', '\uE062', '\uE063', '\uE064', '\uE065', '\uE066', '\uE067',
            '\uE068', '\uE069');
    private static final List<Character> fancySpecialCharacters =
            List.of('\uE06A', '\uE06B', '\uE06C', '\uE06D', '\uE06E', '\uE06F');
    private static final Map<Character, Character> normalToFancy = new HashMap<>();
    private static final Map<Character, Character> normalToBackground = new HashMap<>();

    public WynnFontModel() {
        super(List.of());

        createFontMaps();
        registerFontsForLookup();
    }

    public String toBackgroundFont(
            String text, CustomColor textColor, CustomColor backgroundColor, String leftEdge, String rightEdge) {
        StringBuilder sb = new StringBuilder();
        BackgroundEdge left = BackgroundEdge.fromString(leftEdge);
        BackgroundEdge right = BackgroundEdge.fromString(rightEdge);

        // Tracks whether we are currently "inside" the background
        boolean inBackground = false;
        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') {
                if (inBackground) {
                    sb.append("§")
                            .append(backgroundColor.toHexString())
                            .append(BACKGROUND)
                            .append(NEGATIVE_SPACE)
                            .append(' ');
                } else {
                    sb.append("§").append(textColor.toHexString()).append(' ');
                }
                continue;
            }

            Character fancy = normalToBackground.get(c);

            // If the character is not present in the map then we add the normal version but we also need to close the
            // current background if we are currently inside it.
            if (fancy != null) {
                if (!inBackground) {
                    if (left != BackgroundEdge.NONE) {
                        sb.append("§")
                                .append(backgroundColor.toHexString())
                                .append(left.getLeft())
                                .append(NEGATIVE_SPACE_EDGE);
                    }
                    inBackground = true;
                }
                sb.append("§")
                        .append(backgroundColor.toHexString())
                        .append(BACKGROUND)
                        .append(NEGATIVE_SPACE);
                sb.append("§").append(textColor.toHexString()).append(fancy);
            } else {
                if (inBackground) {
                    if (right != BackgroundEdge.NONE) {
                        sb.append("§")
                                .append(backgroundColor.toHexString())
                                .append(NEGATIVE_SPACE_EDGE)
                                .append(right.getRight());
                    }
                    inBackground = false;
                }
                sb.append("§").append(textColor.toHexString()).append(c);
            }
        }

        // If we are in a background then we need to close it.
        if (inBackground && right != BackgroundEdge.NONE) {
            sb.append("§")
                    .append(backgroundColor.toHexString())
                    .append(NEGATIVE_SPACE_EDGE)
                    .append(right.getRight());
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
            Character fancy = normalToFancy.getOrDefault(c, c);
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

    private void registerWynnFont(String fontName, String code) {
        FontLookup.registerFontCode(ResourceLocation.tryParse(fontName), code);
    }

    private void registerFontsForLookup() {
        registerWynnFont("minecraft:default", "d");
        registerWynnFont("minecraft:banner/pill", "bp");
        registerWynnFont("minecraft:chat/prefix", "cp");
        registerWynnFont("minecraft:hud/gameplay/default/top_left", "gtl");
        registerWynnFont("minecraft:hud/gameplay/default/top_middle", "gtm");
        registerWynnFont("minecraft:hud/gameplay/default/top_right", "gtr");
        registerWynnFont("minecraft:hud/gameplay/default/center_left", "gcl");
        registerWynnFont("minecraft:hud/gameplay/default/center_middle", "gcm");
        registerWynnFont("minecraft:hud/gameplay/default/center_right", "gcr");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_left", "gbl");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_middle", "gbm");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_right", "gbr");
        registerWynnFont("minecraft:hud/selector/default/top_left", "stl");
        registerWynnFont("minecraft:hud/selector/default/top_middle", "stm");
        registerWynnFont("minecraft:hud/selector/default/top_right", "str");
        registerWynnFont("minecraft:hud/selector/default/center_left", "scl");
        registerWynnFont("minecraft:hud/selector/default/center_middle", "scm");
        registerWynnFont("minecraft:hud/selector/default/center_right", "scr");
        registerWynnFont("minecraft:hud/selector/default/bottom_left", "sbl");
        registerWynnFont("minecraft:hud/selector/default/bottom_middle", "sbm");
        registerWynnFont("minecraft:hud/selector/default/bottom_right", "sbr");
    }
}

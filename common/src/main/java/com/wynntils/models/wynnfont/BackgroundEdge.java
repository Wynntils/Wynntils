/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnfont;

import com.wynntils.utils.type.Pair;

public enum BackgroundEdge {
    NONE("NONE", null),
    PILL("PILL", new Pair<>('\uE010', '\uE011')),
    BOX("BOX", new Pair<>('\uE00C', '\uE00D')),
    FLAG("FLAG", new Pair<>('\uE00A', '\uE00B')),
    RIBBON("RIBBON", new Pair<>('\uE008', '\uE009'));

    private final String edgeType;
    private final Pair<Character, Character> characterPair;

    BackgroundEdge(String edgeType, Pair<Character, Character> characterPair) {
        this.edgeType = edgeType;
        this.characterPair = characterPair;
    }

    public static BackgroundEdge fromString(String type) {
        for (BackgroundEdge edge : values()) {
            if (edge.edgeType.equalsIgnoreCase(type)) return edge;
        }
        return NONE;
    }

    public Character getLeft() {
        return characterPair.a();
    }

    public Character getRight() {
        return characterPair.b();
    }
}

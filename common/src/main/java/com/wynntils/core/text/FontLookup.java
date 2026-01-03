/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class FontLookup {
    private static final Map<FontDescription.Resource, String> FONT_TO_CODE_MAP = new HashMap<>();
    private static final Map<String, FontDescription.Resource> CODE_TO_FONT_MAP = new HashMap<>();

    public static void registerFontCode(FontDescription.Resource font, String code) {
        FONT_TO_CODE_MAP.put(font, code);
        CODE_TO_FONT_MAP.put(code, font);
    }

    public static String getFontCodeFromFont(FontDescription.Resource font) {
        return FONT_TO_CODE_MAP.computeIfAbsent(
                font, (resource) -> resource.id().toString());
    }

    public static FontDescription.Resource getFontFromFromFontCode(String fontCode) {
        return CODE_TO_FONT_MAP.computeIfAbsent(
                fontCode,
                // If we did not find a code, assume we have the full font name
                // If this does not work we're screwed, just return the default font
                fc -> Optional.of(new FontDescription.Resource(Identifier.tryParse(fc)))
                        .orElseGet(() ->
                                new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "default"))));
    }
}

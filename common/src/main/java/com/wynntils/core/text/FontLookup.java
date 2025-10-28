/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.ResourceLocation;

public class FontLookup {
    private static final Map<FontDescription, String> FONT_TO_CODE_MAP = new HashMap<>();
    private static final Map<String, FontDescription> CODE_TO_FONT_MAP = new HashMap<>();

    public static void registerFontCode(FontDescription font, String code) {
        FONT_TO_CODE_MAP.put(font, code);
        CODE_TO_FONT_MAP.put(code, font);
    }

    public static String getFontCodeFromFont(FontDescription font) {
        return FONT_TO_CODE_MAP.computeIfAbsent(font, FontDescription::toString);
    }

    public static FontDescription getFontFromFromFontCode(String fontCode) {
        return CODE_TO_FONT_MAP.computeIfAbsent(
                fontCode,
                // If we did not find a code, assume we have the full font name
                // If this does not work we're screwed, just return the default font
                fc -> Optional.of(new FontDescription.Resource(ResourceLocation.tryParse(fc)))
                        .orElseGet(() -> new FontDescription.Resource(
                                ResourceLocation.fromNamespaceAndPath("minecraft", "default"))));
    }
}

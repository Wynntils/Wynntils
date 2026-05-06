/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import java.util.Map;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public record FontEntry(String key, String fontId, Map<String, String> glyphs) {
    public FontDescription.Resource font() {
        return new FontDescription.Resource(Identifier.tryParse(fontId));
    }
}

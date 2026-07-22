/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class CommonFonts {
    public static final FontDescription DEFAULT_FONT = font("default");
    public static final FontDescription LANGUAGE_FONT = font("language/wynncraft");
    public static final FontDescription SPACE_FONT = font("space");
    public static final FontDescription EMBLEM_FRAME_FONT = font("tooltip/emblem/frame");
    public static final FontDescription EMBLEM_SPRITE_FONT = font("tooltip/emblem/sprite");
    public static final FontDescription ATTRIBUTE_SPRITE_FONT = font("tooltip/attribute/sprite");
    public static final FontDescription TOOLTIP_BANNER_FONT = font("tooltip/banner");
    public static final FontDescription REQUIREMENT_FRAME_FONT = font("tooltip/requirement/frame");
    public static final FontDescription REQUIREMENT_SPRITE_FONT = font("tooltip/requirement/sprite");
    public static final FontDescription DIVIDER_FONT = font("tooltip/divider");
    public static final FontDescription MAJOR_ID_FONT = font("tooltip/identification/major");
    public static final FontDescription PAGE_FONT = font("tooltip/page");
    public static final FontDescription CHAT_TILE_FONT = font("chat/tile");
    public static final FontDescription COMMON_FONT = font("common");
    public static final FontDescription QUAD_12 = font("offset/wynncraft_quad/12");
    public static final FontDescription IDENTIFICATION_DIVIDER_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("wynntils", "tooltip_identification_divider"));

    private static FontDescription font(String path) {
        return new FontDescription.Resource(Identifier.withDefaultNamespace(path));
    }
}

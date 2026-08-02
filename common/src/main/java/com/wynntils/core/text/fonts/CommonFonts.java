/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import com.wynntils.core.WynntilsMod;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class CommonFonts {
    public static final FontDescription CHAT_TILE_FONT = font("chat/tile");
    public static final FontDescription COMMON_FONT = font("common");
    public static final FontDescription LANGUAGE_FIVE_FONT = font("language/five");
    public static final FontDescription LANGUAGE_WYNNCRAFT_FONT = font("language/wynncraft");
    public static final FontDescription OFFSET_QUAD_12 = font("offset/wynncraft_quad/12");
    public static final FontDescription MERCHANT_FONT = font("merchant");
    public static final FontDescription PROFESSION_FONT = font("profession");
    public static final FontDescription SPACE_FONT = font("space");
    public static final FontDescription TOOLTIP_ATTRIBUTE_SPRITE_FONT = font("tooltip/attribute/sprite");
    public static final FontDescription TOOLTIP_DIVIDER_FONT = font("tooltip/divider");
    public static final FontDescription TOOLTIP_EMBLEM_FRAME_FONT = font("tooltip/emblem/frame");
    public static final FontDescription TOOLTIP_EMBLEM_SPRITE_FONT = font("tooltip/emblem/sprite");
    public static final FontDescription TOOLTIP_PAGE_FONT = font("tooltip/page");
    public static final FontDescription TOOLTIP_REQUIREMENT_SPRITE_FONT = font("tooltip/requirement/sprite");

    public static final FontDescription WYNNTILS_TOOLTIP = wynntilsFont("tooltip");

    private static FontDescription.Resource font(String path) {
        return new FontDescription.Resource(Identifier.withDefaultNamespace(path));
    }

    private static FontDescription.Resource wynntilsFont(String path) {
        return new FontDescription.Resource(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, path));
    }
}

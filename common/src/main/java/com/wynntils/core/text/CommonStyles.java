/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.core.text.fonts.CommonFonts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public final class CommonStyles {
    public static final Style LANGUAGE =
            Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.WHITE);
    public static final Style SPACE =
            Style.EMPTY.withFont(CommonFonts.SPACE_FONT).withoutShadow();
}

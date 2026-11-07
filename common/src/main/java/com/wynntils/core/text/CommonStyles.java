/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.core.text.fonts.CommonFonts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public final class CommonStyles {
    public static final Style COMMON_WITHOUT_SHADOW =
            Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withoutShadow();
    public static final Style GRAY_LANGUAGE_WYNNCRAFT =
            Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT).withColor(ChatFormatting.GRAY);
    public static final Style WHITE_LANGUAGE_WYNNCRAFT =
            Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT).withColor(ChatFormatting.WHITE);
    public static final Style SPACE =
            Style.EMPTY.withFont(CommonFonts.SPACE_FONT).withoutShadow();
}

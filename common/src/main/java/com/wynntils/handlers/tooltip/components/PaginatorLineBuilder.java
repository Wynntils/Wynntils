/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.FontManager;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class PaginatorLineBuilder {
    private static final CustomColor CURRENT_PAGE = new CustomColor(255, 234, 128);
    private static final CustomColor INACTIVE_PAGE = new CustomColor(69, 84, 73);

    private static final String PAGE_INDICATOR = "\uE000";
    private static final int INDICATOR_SPACING = 4;
    private static final int ICON_SPACING = 7;

    public static TooltipLine buildPaginator(Component icon, int currentPage, int pageCount) {
        MutableComponent footer = Component.empty().withoutShadow();

        for (int page = 0; page < pageCount; page++) {
            if (page > 0) {
                footer.append(space(INDICATOR_SPACING));
            }

            footer.append(Component.literal(PAGE_INDICATOR)
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.TOOLTIP_PAGE_FONT)
                            .withColor((page == currentPage ? CURRENT_PAGE : INACTIVE_PAGE).asInt())));
        }

        int iconWidth = McUtils.mc().font.width(icon);
        footer.append(space(ICON_SPACING)).append(icon).append(space(-(ICON_SPACING + iconWidth)));

        return TooltipLine.center(footer);
    }

    private static Component space(int width) {
        return Component.literal(FontManager.calculateOffset(0, width)).withStyle(CommonStyles.SPACE);
    }
}

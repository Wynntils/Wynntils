/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.gear.type.GearTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class LoreLineBuilder {
    private static final int LORE_MAX_WIDTH = 160;

    public static List<TooltipElement> buildLore(StyledText lore, String creator) {
        List<TooltipElement> lines = new ArrayList<>();

        if (lore.isEmpty()) {
            lines.add(TooltipWrappedText.center(
                    Component.literal("No tales or legends have been recorded about this item")
                            .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT),
                    LORE_MAX_WIDTH));
        } else {
            for (StyledText paragraph : lore.split("\n", true)) {
                if (paragraph.isEmpty()) {
                    lines.add(TooltipLine.empty());
                    continue;
                }

                lines.add(TooltipWrappedText.center(paragraph.getComponent(), LORE_MAX_WIDTH));
            }
        }

        if (!creator.isEmpty()) {
            lines.add(TooltipLine.empty());
            lines.add(TooltipLine.center(Component.literal("Crafted by " + creator)
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(GearTier.CRAFTED.getSecondaryColor().asInt()))));
        }

        return lines;
    }
}

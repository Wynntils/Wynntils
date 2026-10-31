/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.RangedValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

public final class SetBonusLineBuilder {
    public static Component buildSetName(String name, GearTier tier) {
        return Component.empty()
                .append(BannerBoxFont.buildMessage(
                        "set",
                        CustomColor.fromChatFormatting(tier.getChatFormatting()),
                        CommonColors.BLACK,
                        "\uDB00\uDC02\uDAFF\uDFFF"))
                .append(BannerBoxFont.buildMessage(name, tier.getSecondaryColor(), CommonColors.BLACK, ""));
    }

    public static TooltipLine buildHeader(String name, GearTier tier, int equippedCount, int totalCount) {
        return TooltipLine.leftRight(
                buildSetName(name, tier),
                Component.literal(equippedCount + "/" + totalCount).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
    }

    public static List<TooltipElement> buildBonuses(
            Map<StatType, Integer> bonuses, GearRequirements requirements, RangedValue levelRange) {
        Map<StatType, TooltipElement> statLines = new HashMap<>();
        for (Map.Entry<StatType, Integer> entry : bonuses.entrySet()) {
            StatType stat = entry.getKey();

            Component name = IdentificationLineBuilder.buildStatDisplayName(stat, requirements, levelRange, true);
            int value = entry.getValue();
            boolean positive = value > 0 ^ stat.displayAsInverted();

            Component bonus = Component.literal(StringUtils.toSignedCommaString(value)
                            + stat.getUnit().getDisplayName())
                    .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT)
                    .withColor((positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt());
            statLines.put(stat, TooltipLine.leftRight(name, bonus));
        }

        return IdentificationLineBuilder.buildLines(statLines);
    }
}

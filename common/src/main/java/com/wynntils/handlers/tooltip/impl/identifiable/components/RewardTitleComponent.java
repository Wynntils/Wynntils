/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipStyleSupport;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

final class RewardTitleComponent {
    private static final Style WYNNCRAFT_WHITE_STYLE =
            Style.EMPTY.withFont(TooltipStyleSupport.WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE);
    private static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));
    private static final FontDescription EMBLEM_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/sprite"));
    private static final FontDescription ATTRIBUTE_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/attribute/sprite"));
    private static final FontDescription TOOLTIP_BANNER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/banner"));

    public Component buildNameLine(
            GearType gearType, MutableComponent itemNameComponent, boolean unidentified, boolean hideUnidentified) {
        MutableComponent nameLine = Component.empty().withStyle(WYNNCRAFT_WHITE_STYLE);

        MutableComponent emblemComponent = Component.literal("\uDAFF\uDFF0")
                .withStyle(style -> style.withFont(TooltipStyleSupport.WYNNCRAFT_LANGUAGE_FONT)
                        .withShadowColor(0xFFFFFF));
        emblemComponent.append(
                Component.literal(getRewardFrameCode(gearType)).withStyle(Style.EMPTY.withFont(EMBLEM_FRAME_FONT)));
        emblemComponent.append(Component.literal("\uDAFF\uDFCF"));
        emblemComponent.append(Component.empty()
                .withStyle(Style.EMPTY.withColor(0x00eb1c))
                .append(Component.literal(getRewardSpriteCode(gearType))
                        .withStyle(Style.EMPTY.withFont(EMBLEM_SPRITE_FONT))));
        nameLine.append(emblemComponent);

        nameLine.append(Component.literal("\uDB00\uDC05").withStyle(TooltipStyleSupport.SPACING_STYLE));

        if (unidentified && !hideUnidentified) {
            nameLine.append(TooltipStyleSupport.withWhiteShadow(
                    Component.literal("\uE008").withStyle(Style.EMPTY.withFont(ATTRIBUTE_SPRITE_FONT))));
            nameLine.append(Component.literal("\uDB00\uDC02").withStyle(TooltipStyleSupport.SPACING_STYLE));
        }

        nameLine.append(itemNameComponent);
        return nameLine;
    }

    public Component buildTagsLine(GearTier gearTier, String typeName, GearRestrictions restrictions) {
        MutableComponent rarityTypeLine = Component.empty().withStyle(WYNNCRAFT_WHITE_STYLE);
        rarityTypeLine.append(Component.literal("\uDB00\uDC26").withStyle(TooltipStyleSupport.SPACING_STYLE));
        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearTier.getName(),
                CustomColor.fromChatFormatting(gearTier.getChatFormatting()),
                CommonColors.BLACK,
                "\uDB00\uDC02"));
        rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(TooltipStyleSupport.SPACING_STYLE));

        boolean untradable = restrictions == GearRestrictions.UNTRADABLE;
        rarityTypeLine.append(BannerBoxFont.buildMessage(
                typeName, getSecondaryTierColor(gearTier), CommonColors.BLACK, untradable ? "\uDB00\uDC02" : ""));

        if (untradable) {
            rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(TooltipStyleSupport.SPACING_STYLE));
            rarityTypeLine.append(buildRestrictionIcon());
        }

        return rarityTypeLine;
    }

    private static MutableComponent buildRestrictionIcon() {
        MutableComponent restrictionIcon = Component.empty().withStyle(WYNNCRAFT_WHITE_STYLE);
        restrictionIcon.append(Component.literal("\uE002")
                .withStyle(Style.EMPTY.withFont(TOOLTIP_BANNER_FONT).withColor(0xff4242)));
        restrictionIcon.append(
                Component.literal("\uDAFF\uDFF6\uF002").withStyle(Style.EMPTY.withFont(TOOLTIP_BANNER_FONT)));
        return TooltipStyleSupport.withWhiteShadow(restrictionIcon);
    }

    private static String getRewardFrameCode(GearType gearType) {
        return switch (gearType) {
            case CHARM -> "\uE035";
            case MASTERY_TOME -> "\uE041";
            default -> gearType.getFrameCode();
        };
    }

    private static String getRewardSpriteCode(GearType gearType) {
        return switch (gearType) {
            case CHARM -> "\uE031";
            case MASTERY_TOME -> "\uE028";
            default -> gearType.getFrameSpriteCode();
        };
    }

    private static CustomColor getSecondaryTierColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xcff9f9);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            default -> CustomColor.NONE;
        };
    }
}

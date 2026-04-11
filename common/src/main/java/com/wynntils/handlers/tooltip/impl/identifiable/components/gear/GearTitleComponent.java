/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GearTitleComponent {
    public List<Component> buildHeaderLines(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();
        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        String setName = gearInfo.setInfo().map(SetInfo::name).orElse("");

        header.add(Component.empty());
        header.add(buildNameLine(gearInfo, gearInstance, hideUnidentified, feature));

        MutableComponent rarityTypeLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        rarityTypeLine.append(Component.literal("\uDB00\uDC26")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));
        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.tier().getName(),
                CustomColor.fromChatFormatting(gearInfo.tier().getChatFormatting()),
                CommonColors.BLACK,
                "\uDB00\uDC02"));
        rarityTypeLine.append(Component.literal("\uDB00\uDC01")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));

        boolean untradable = gearInfo.metaInfo().restrictions() == GearRestrictions.UNTRADABLE;
        CustomColor secondaryTierColor = GearTooltipSupport.getSecondaryTierColor(gearInfo.tier());
        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.type().name(), secondaryTierColor, CommonColors.BLACK, untradable ? "\uDB00\uDC02" : ""));

        if (untradable) {
            rarityTypeLine.append(Component.literal("\uDB00\uDC01")
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));

            MutableComponent restrictionIcon = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            restrictionIcon.append(Component.literal("\uE002")
                    .withStyle(Style.EMPTY
                            .withFont(GearTooltipSupport.TOOLTIP_BANNER_FONT)
                            .withColor(0xff4242)));
            restrictionIcon.append(Component.literal("\uDAFF\uDFF6\uF002")
                    .withStyle(Style.EMPTY.withFont(GearTooltipSupport.TOOLTIP_BANNER_FONT)));
            rarityTypeLine.append(GearTooltipSupport.withWhiteShadow(restrictionIcon));
        }

        header.add(rarityTypeLine);

        List<Element> itemElements = collectItemElements(gearInfo);
        if (!setName.isBlank() || !itemElements.isEmpty()) {
            MutableComponent tagsLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);

            if (!setName.isBlank()) {
                tagsLine.append(
                        BannerBoxFont.buildMessage(setName + " set", secondaryTierColor, CommonColors.BLACK, ""));
            }

            if (!itemElements.isEmpty()) {
                if (!setName.isBlank()) {
                    tagsLine.append(Component.literal("\uDB00\uDC01")
                            .withStyle(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .SPACING_STYLE));
                }
                tagsLine.append(buildElementStrip(itemElements, secondaryTierColor));
            }

            header.add(tagsLine);
        }

        header.add(Component.empty());
        return header;
    }

    public Component buildNameLine(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        return buildNameLine(
                gearInfo,
                gearInstance,
                hideUnidentified,
                Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class));
    }

    private Component buildNameLine(
            GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified, ItemStatInfoFeature feature) {
        MutableComponent nameLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);

        String frameCode = gearInfo.type().getFrameCode();
        String spriteCode = gearInfo.type().getFrameSpriteCode();
        String setName = gearInfo.setInfo().map(SetInfo::name).orElse("");

        if (!setName.isEmpty()) {
            frameCode = String.valueOf((char) (frameCode.charAt(0) + 0x1000));
            spriteCode = String.valueOf((char) (spriteCode.charAt(0) + 0x1000));
        }

        MutableComponent emblemComponent = Component.literal("\uDAFF\uDFF0").withStyle(style -> style.withFont(
                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                .WYNNCRAFT_LANGUAGE_FONT)
                .withShadowColor(0xFFFFFF));
        emblemComponent.append(
                Component.literal(frameCode).withStyle(Style.EMPTY.withFont(GearTooltipSupport.EMBLEM_FRAME_FONT)));
        emblemComponent.append(Component.literal("\uDAFF\uDFCF"));
        emblemComponent.append(Component.empty()
                .withStyle(Style.EMPTY.withColor(0x00eb1c))
                .append(Component.literal(spriteCode)
                        .withStyle(Style.EMPTY.withFont(GearTooltipSupport.EMBLEM_SPRITE_FONT))));
        nameLine.append(emblemComponent);

        nameLine.append(Component.literal("\uDB00\uDC05")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));

        if (gearInstance == null && !hideUnidentified) {
            nameLine.append(GearTooltipSupport.withWhiteShadow(Component.literal("\uE008")
                    .withStyle(Style.EMPTY.withFont(GearTooltipSupport.ATTRIBUTE_SPRITE_FONT))));
            nameLine.append(Component.literal("\uDB00\uDC02")
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));
        }

        MutableComponent itemNameComponent = buildItemNameComponent(gearInfo, gearInstance, feature);
        appendOverallPercentage(itemNameComponent, gearInstance, feature);
        nameLine.append(itemNameComponent);
        return nameLine;
    }

    private MutableComponent buildItemNameComponent(
            GearInfo gearInfo, GearInstance gearInstance, ItemStatInfoFeature feature) {
        boolean isShiny = gearInstance != null && gearInstance.shinyStat().isPresent();
        String itemName = isShiny ? "Shiny " + gearInfo.name() : gearInfo.name();

        MutableComponent nameComponent;
        if (feature.perfect.get() && gearInstance != null && gearInstance.isPerfect()) {
            nameComponent = ComponentUtils.makeRainbowStyle("Perfect " + itemName, true);
        } else if (feature.defective.get() && gearInstance != null && gearInstance.isDefective()) {
            nameComponent = ComponentUtils.makeCrimsonStyle("Defective " + itemName, true);
        } else {
            nameComponent = Component.literal(itemName)
                    .withStyle(Style.EMPTY
                            .withFont(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(gearInfo.tier().getChatFormatting()));
        }

        return nameComponent;
    }

    private static void appendOverallPercentage(
            MutableComponent itemNameComponent, GearInstance gearInstance, ItemStatInfoFeature feature) {
        if (!shouldAppendOverallPercentage(gearInstance, feature)) {
            return;
        }

        itemNameComponent.append(ColorScaleUtils.getPercentageTextComponent(
                        feature.getColorMap(),
                        gearInstance.getOverallPercentage(),
                        feature.colorLerp.get(),
                        feature.decimalPlaces.get())
                .withStyle(style -> style.withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)));
    }

    static boolean shouldAppendOverallPercentage(GearInstance gearInstance, ItemStatInfoFeature feature) {
        if (gearInstance == null || !gearInstance.hasOverallValue()) {
            return false;
        }

        boolean specialTitle = feature.perfect.get() && gearInstance.isPerfect()
                || feature.defective.get() && gearInstance.isDefective();
        if (specialTitle) {
            return feature.overallPercentageInPerfectDefectiveName.get();
        }

        return feature.overallPercentageInName.get();
    }

    private static List<Element> collectItemElements(GearInfo gearInfo) {
        Set<Element> seenElements = new LinkedHashSet<>();
        for (Pair<DamageType, RangedValue> damage : gearInfo.fixedStats().damages()) {
            damage.a().getElement().ifPresent(seenElements::add);
        }

        return seenElements.stream()
                .sorted((left, right) -> Integer.compare(left.getEncodingId(), right.getEncodingId()))
                .toList();
    }

    private static MutableComponent buildElementStrip(List<Element> elements, CustomColor dividerColor) {
        MutableComponent strip = Component.literal("\uDB00\uDC26")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(dividerColor.asInt())
                        .withShadowColor(0xFFFFFF));

        for (int i = 0; i < elements.size(); i++) {
            char elementGlyph = (char) ('\uE006' + elements.get(i).getEncodingId());
            String glyph = String.valueOf(elementGlyph);

            strip.append(
                    Component.literal(glyph).withStyle(Style.EMPTY.withFont(GearTooltipSupport.TOOLTIP_BANNER_FONT)));
            strip.append(Component.empty()
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                    .append(Component.literal("\uDAFF\uDFF6" + glyph)
                            .withStyle(Style.EMPTY.withFont(GearTooltipSupport.TOOLTIP_BANNER_FONT))));

            if (i < elements.size() - 1) {
                strip.append(Component.literal("\uDAFF\uDFFF"));
            }
        }

        return strip;
    }
}

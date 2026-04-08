/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class GearOverviewComponent {
    public List<Component> buildHeaderLines(GearInfo gearInfo) {
        List<Component> header = new ArrayList<>();

        if (gearInfo.type().isArmor() || gearInfo.type().isAccessory()) {
            MutableComponent healthLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            healthLine.append(Component.literal("\uDB00\uDC02")
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));
            healthLine.append(Component.literal(StringUtils.toSignedCommaString(
                            gearInfo.fixedStats().healthBuff()))
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                            .withColor(GearTooltipSupport.getSecondaryTierColor(gearInfo.tier())
                                    .asInt())));
            healthLine.append(Component.literal(" Health")
                    .withStyle(Style.EMPTY.withFont(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .WYNNCRAFT_LANGUAGE_FONT)));
            header.add(healthLine);
        } else if (gearInfo.type().isWeapon()) {
            MutableComponent dpsLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            dpsLine.append(Component.literal("\uDB00\uDC02")
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));
            dpsLine.append(Component.literal(String.format("%,d", GearTooltipSupport.getDisplayedDps(gearInfo)))
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                            .withColor(GearTooltipSupport.getSecondaryTierColor(gearInfo.tier())
                                    .asInt())));
            dpsLine.append(Component.literal(" DPS")
                    .withStyle(Style.EMPTY.withFont(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .WYNNCRAFT_LANGUAGE_FONT)));
            header.add(dpsLine);
        }

        if (gearInfo.fixedStats().attackSpeed().isPresent()) {
            MutableComponent attackSpeedLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            attackSpeedLine.append(Component.literal("\uDB00\uDC02")
                    .withStyle(
                            com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                    .SPACING_STYLE));
            attackSpeedLine.append(GearTooltipSupport.withWhiteShadow(Component.literal("\uE007")
                    .withStyle(Style.EMPTY.withFont(GearTooltipSupport.ATTRIBUTE_SPRITE_FONT))));
            attackSpeedLine.append(Component.literal(
                            " " + gearInfo.fixedStats().attackSpeed().get().getName() + " ")
                    .withStyle(Style.EMPTY
                            .withFont(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.GRAY)));
            attackSpeedLine.append(Component.literal(
                            "(" + gearInfo.fixedStats().attackSpeed().get().getHitsPerSecond() + " hits/s)")
                    .withStyle(Style.EMPTY
                            .withFont(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.DARK_GRAY)));
            header.add(attackSpeedLine);
        }

        appendDefences(header, gearInfo);
        appendDamages(header, gearInfo);
        return header;
    }

    private static void appendDefences(List<Component> header, GearInfo gearInfo) {
        List<Pair<Element, Integer>> defenses = gearInfo.fixedStats().defences();
        if (defenses.isEmpty()) {
            return;
        }

        MutableComponent defensesHeader = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        defensesHeader.append(Component.literal("\uDB00\uDC02")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));
        defensesHeader.append(Component.literal("Elemental Defences")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.GRAY)));
        header.add(defensesHeader);

        MutableComponent defenseLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        for (int i = 0; i < defenses.size(); i++) {
            if (i == 0 || i == 3) {
                defenseLine.append(Component.literal("\uDB00\uDC02")
                        .withStyle(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .SPACING_STYLE));
            }

            Pair<Element, Integer> defenseStat = defenses.get(i);
            MutableComponent elementComponent = Component.empty();
            String elementSymbol = defenseStat.a() == Element.EARTH
                    ? "\uDAFF\uDFFF" + defenseStat.a().getTooltipSprite()
                    : defenseStat.a().getTooltipSprite();

            if (defenseStat.a() == Element.EARTH
                    || defenseStat.a() == Element.THUNDER
                    || defenseStat.a() == Element.AIR) {
                elementSymbol += "\uDAFF\uDFFF";
            }

            elementComponent.append(GearTooltipSupport.withWhiteShadow(Component.empty()
                    .append(Component.literal(elementSymbol)
                            .withStyle(Style.EMPTY
                                    .withFont(GearTooltipSupport.ATTRIBUTE_SPRITE_FONT)
                                    .withColor(ChatFormatting.WHITE)))
                    .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))));
            elementComponent.append(Component.literal(StringUtils.toSignedCommaString(defenseStat.b()))
                    .withStyle(Style.EMPTY
                            .withFont(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.GRAY)));

            int width = McUtils.mc().font.width(elementComponent);
            if (width < GearTooltipSupport.ELEMENTAL_DEFENSES_WIDTH) {
                String offset = Managers.Font.calculateOffset(width, GearTooltipSupport.ELEMENTAL_DEFENSES_WIDTH);
                elementComponent.append(Component.literal(offset)
                        .withStyle(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .SPACING_STYLE)
                        .withStyle(ChatFormatting.GRAY));
            }

            boolean lastInRow = i == 2 || i == defenses.size() - 1;
            if (!lastInRow) {
                elementComponent.append(Component.literal(" ")
                        .withStyle(Style.EMPTY
                                .withFont(
                                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                                .WYNNCRAFT_LANGUAGE_FONT)
                                .withColor(ChatFormatting.GRAY)));
            }

            defenseLine.append(elementComponent);
            if (i == defenses.size() - 1 || i == 2) {
                header.add(defenseLine);
                defenseLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            }
        }
    }

    private static void appendDamages(List<Component> header, GearInfo gearInfo) {
        List<Pair<DamageType, RangedValue>> damages = gearInfo.fixedStats().damages();
        if (damages.isEmpty()) {
            return;
        }

        MutableComponent damageLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        for (int i = 0; i < damages.size(); i++) {
            if (i == 0 || i == 3) {
                damageLine.append(Component.literal("\uDB00\uDC02")
                        .withStyle(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .SPACING_STYLE));
            }

            Pair<DamageType, RangedValue> damageStat = damages.get(i);
            Element element = damageStat.a().getElement().orElse(null);
            MutableComponent elementComponent = Component.empty();
            String elementSymbol = element == Element.EARTH || damageStat.a() == DamageType.NEUTRAL
                    ? "\uDAFF\uDFFF" + damageStat.a().getTooltipSprite()
                    : damageStat.a().getTooltipSprite();

            if (damageStat.a() == DamageType.NEUTRAL
                    || element == Element.EARTH
                    || element == Element.THUNDER
                    || element == Element.AIR) {
                elementSymbol += "\uDAFF\uDFFF";
            }

            elementComponent.append(GearTooltipSupport.withWhiteShadow(Component.empty()
                    .append(Component.literal(elementSymbol)
                            .withStyle(Style.EMPTY
                                    .withFont(GearTooltipSupport.ATTRIBUTE_SPRITE_FONT)
                                    .withColor(ChatFormatting.WHITE)))
                    .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))));
            elementComponent.append(Component.literal(damageStat.b().asString())
                    .withStyle(Style.EMPTY
                            .withFont(
                                    com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                            .WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.GRAY)));

            boolean lastInRow = i == 2 || i == damages.size() - 1;
            if (!lastInRow) {
                elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
            }

            damageLine.append(elementComponent);
            if (i == damages.size() - 1 || i == 2) {
                header.add(damageLine);
                damageLine = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
            }
        }
    }
}

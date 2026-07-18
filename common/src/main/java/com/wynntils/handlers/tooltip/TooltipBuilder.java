/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public abstract class TooltipBuilder {
    private static final TooltipStyle DEFAULT_TOOLTIP_STYLE =
            new TooltipStyle(StatListOrdering.WYNNCRAFT, false, false, true, true);

    private final List<Component> header;
    private final List<Component> footer;
    private final String source;
    private List<Component> tooltipLinesCache;

    protected TooltipBuilder(List<Component> header, List<Component> footer, String source) {
        this.header = List.copyOf(header);
        this.footer = List.copyOf(footer);
        this.source = source;
    }

    public List<Component> getTooltipLines(ClassType currentClass) {
        return getTooltipLines(currentClass, DEFAULT_TOOLTIP_STYLE, null, ItemWeightSource.NONE, null);
    }

    public List<Component> getTooltipLines(ClassType currentClass, TooltipOptions options) {
        return getTooltipLines(currentClass, options.style(), null, options.itemWeightSource(), null);
    }

    public List<Component> getTooltipLines(
            ClassType currentClass,
            TooltipStyle style,
            TooltipIdentificationDecorator identificationDecorator,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        if (tooltipLinesCache == null) {
            List<Component> decoratedHeader = decorateHeader(header, identificationDecorator);
            int targetWidth = 0;
            for (Component line : decoratedHeader) {
                targetWidth = Math.max(targetWidth, McUtils.mc().font.width(line));
            }
            for (Component line : footer) {
                targetWidth = Math.max(targetWidth, McUtils.mc().font.width(line));
            }

            List<Component> tooltip = new ArrayList<>();
            tooltip.addAll(decoratedHeader);
            tooltip.addAll(getIdentificationLines(currentClass, style, identificationDecorator, targetWidth));
            tooltip.addAll(footer);
            tooltipLinesCache = prependSource(tooltip);
        }

        return tooltipLinesCache;
    }

    private Component buildSourceLine() {
        return Component.empty()
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).applyFormat(getSourceColor()))
                .append(Component.literal("\uE000").withStyle(Style.EMPTY.withFont(CommonFonts.MAJOR_ID_FONT)))
                .append(Component.literal("\uDB00\uDC02"))
                .append(Component.literal(source)
                        .withStyle(style ->
                                style.withFont(CommonFonts.LANGUAGE_FONT).applyFormat(ChatFormatting.WHITE)));
    }

    protected ChatFormatting getSourceColor() {
        return ChatFormatting.WHITE;
    }

    protected List<Component> prependSource(List<Component> lines) {
        if (source.isEmpty()) return List.copyOf(lines);

        List<Component> tooltip = new ArrayList<>(lines.size() + 1);
        tooltip.add(buildSourceLine());
        tooltip.addAll(lines);
        return List.copyOf(tooltip);
    }

    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator, int targetWidth) {
        return List.of();
    }

    protected List<Component> decorateHeader(
            List<Component> header, TooltipIdentificationDecorator identificationDecorator) {
        return header;
    }

    protected static Pair<List<Component>, List<Component>> extractHeaderAndFooter(List<Component> lore) {
        List<Component> header = new ArrayList<>();
        List<Component> footer = new ArrayList<>();

        boolean headerEnded = false;
        boolean footerStarted = false;
        boolean skillPointsStarted = false;

        boolean foundSkills = false;
        boolean foundIdentifications = false;
        for (Component loreLine : lore) {
            StyledText codedLine = StyledText.fromComponent(loreLine).getNormalized();

            if (!footerStarted) {
                if (codedLine.matches(WynnItemParser.SET_BONUS_PATTERN)) {
                    headerEnded = true;
                    footerStarted = true;
                } else {
                    Matcher matcher = codedLine.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN);
                    if (matcher.matches()) {
                        skillPointsStarted = false;

                        String statName = matcher.group("statName");
                        if (Skill.isSkill(statName)) {
                            skillPointsStarted = true;
                            foundSkills = true;
                        } else {
                            foundIdentifications = true;
                        }

                        headerEnded = true;
                        continue;
                    } else if (skillPointsStarted) {
                        skillPointsStarted = false;
                        continue;
                    }
                }
            }

            if (!headerEnded) {
                header.add(loreLine);
            } else {
                footerStarted = true;
                footer.add(loreLine);
            }
        }

        if (foundSkills && !foundIdentifications) {
            footer.addFirst(Component.literal(""));
        }

        return Pair.of(header, footer);
    }
}

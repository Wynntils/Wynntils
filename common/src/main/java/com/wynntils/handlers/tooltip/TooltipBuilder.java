/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public abstract class TooltipBuilder {
    private static final TooltipStyle DEFAULT_TOOLTIP_STYLE =
            new TooltipStyle(StatListOrdering.WYNNCRAFT, false, false, true, false, true);

    private final List<Component> header;
    private final List<Component> footer;
    private final String source;
    protected final boolean synthetic;

    private List<Component> tooltipLinesCache;

    protected TooltipBuilder(List<Component> header, List<Component> footer, String source, boolean synthetic) {
        this.header = List.copyOf(header);
        this.footer = List.copyOf(footer);
        this.source = source;
        this.synthetic = synthetic;
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
            tooltipLinesCache = buildTooltipLines(currentClass, style, identificationDecorator);
        }

        return tooltipLinesCache;
    }

    protected List<Component> buildTooltipLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator identificationDecorator) {
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
        return prependSource(postProcessTooltipLines(tooltip));
    }

    private Component buildSourceLine() {
        return Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .applyFormat(getSourceColor()))
                .append(Component.literal("\uE004").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS)))
                .append(Component.literal("\uDB00\uDC02"))
                .append(Component.literal(source)
                        .withStyle(style -> style.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .applyFormat(ChatFormatting.WHITE)));
    }

    protected List<TooltipLine> buildPaginationLines(PagedItemProperty item) {
        int currentPage = item.currentPage();
        MutableComponent keyPrompt = Component.literal(synthetic ? "\uE001" : "\uF002")
                .withStyle(Style.EMPTY.withFont(
                        synthetic ? CommonFonts.WYNNTILS_TOOLTIP_ICONS : CommonFonts.CHAT_TILE_FONT))
                .append(Component.literal("\uDAFF\uDF98\uDB00\uDC3F").withStyle(CommonStyles.LANGUAGE));
        int keyPromptAdvance = McUtils.mc().font.width(keyPrompt);
        MutableComponent paginator = Component.empty().append(keyPrompt);
        for (int page = 0; page < 3; page++) {
            paginator.append(Component.literal("\uE000")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.TOOLTIP_PAGE_FONT)
                            .withColor(page == currentPage ? 0xffea80 : 0x455449)
                            .withShadowColor(0xffffff)));
            if (page < 2) paginator.append(Component.literal("\uDB00\uDC04").withStyle(CommonStyles.LANGUAGE));
        }
        paginator.append(Component.literal(Managers.Font.calculateOffset(0, keyPromptAdvance))
                .withStyle(CommonStyles.SPACE));
        return List.of(new TooltipLine.Centered(paginator), new TooltipLine.Fixed(Component.empty()));
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

    protected List<Component> postProcessTooltipLines(List<Component> tooltip) {
        return List.copyOf(tooltip);
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

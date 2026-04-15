/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.TooltipStyleSupport;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class GearParsedTooltipComponent {
    private static final String SHINY_STAT_ICON = "\uE04F";
    // Keep this in sync with WynnItemParser.SHINY_STAT_PATTERN.
    private static final Pattern SHINY_STAT_PATTERN = Pattern.compile(
            "^§f\uE04F\uDAFF\uDFFF§#(?:[a-f0-9]{8}) ([a-zA-Z ]+).+?§f([\\d,]+)§#(?:[a-f0-9]{8})(\uDB00\uDC00|.+)$");
    private static final String SKILL_REQ_PART = ".+?(\\uE005|\\uE006|\\uE007).+?(?:§8|§#acfac6ff|§#faacacff)(\\d+).+?";
    private static final Pattern SKILL_REQ_PATTERN = Pattern.compile("(?:" + SKILL_REQ_PART + "){5}");
    private static final Pattern SKILL_REQ_PART_PATTERN = Pattern.compile(SKILL_REQ_PART);

    private final GearRequirementsComponent requirementsComponent = new GearRequirementsComponent();
    private final GearTitleComponent titleComponent = new GearTitleComponent();

    private static final FontDescription DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));
    private static final FontDescription IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));
    private static final FontDescription IDENTIFICATION_MAJOR_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/major"));
    private static final FontDescription REQUIREMENT_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/frame"));
    private static final FontDescription REQUIREMENT_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/sprite"));
    private static final FontDescription PAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/page"));

    public IdentifiableTooltipComponent.TooltipParts buildTooltipParts(
            ItemStack itemStack, IdentifiableItemProperty<GearInfo, GearInstance> itemProperty) {
        List<Component> tooltipLines = LoreUtils.getTooltipLines(itemStack);
        if (tooltipLines.isEmpty()) {
            return null;
        }

        if (itemProperty.getItemInstance().isEmpty()) {
            return buildUnidentifiedTooltipParts(tooltipLines, itemProperty);
        }

        int firstIdentificationLine = -1;
        int lastIdentificationLine = -1;

        for (int i = 0; i < tooltipLines.size(); i++) {
            if (!isIdentificationLine(tooltipLines.get(i))) {
                continue;
            }

            if (firstIdentificationLine < 0) {
                firstIdentificationLine = i;
            }
            lastIdentificationLine = i;
        }

        if (firstIdentificationLine < 0 || lastIdentificationLine < firstIdentificationLine) {
            return null;
        }

        final int firstIdentificationIndex = firstIdentificationLine;
        int identificationDividerLine = findLastDividerBefore(tooltipLines, firstIdentificationIndex);
        Component originalShinyRerollTracker = extractOriginalShinyRerollTracker(tooltipLines);
        List<Component> header = buildHeaderWithSyntheticRequirements(
                tooltipLines,
                itemProperty.getItemInfo(),
                itemProperty.getItemInstance().orElse(null),
                identificationDividerLine,
                originalShinyRerollTracker);
        List<Component> footer = copyMarkedRange(
                tooltipLines,
                lastIdentificationLine + 1,
                tooltipLines.size(),
                lineIndex -> classifyFooterMarker(tooltipLines.get(lineIndex)));
        return new IdentifiableTooltipComponent.TooltipParts(header, footer);
    }

    private static boolean isIdentificationLine(Component line) {
        StyledText styledText = StyledText.fromComponent(line);
        if (containsFont(styledText, IDENTIFICATION_MAJOR_FONT) || containsFont(styledText, PAGE_FONT)) {
            return false;
        }

        if (containsFont(styledText, IDENTIFICATION_METER_FONT)) {
            return true;
        }

        StyledText normalized = styledText.getNormalized();
        return normalized.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN).matches();
    }

    private static boolean containsFont(StyledText styledText, FontDescription font) {
        for (StyledTextPart part : styledText) {
            if (font.equals(part.getPartStyle().getFont())) {
                return true;
            }
        }

        return false;
    }

    private IdentifiableTooltipComponent.TooltipParts buildUnidentifiedTooltipParts(
            List<Component> tooltipLines, IdentifiableItemProperty<GearInfo, GearInstance> itemProperty) {
        int pageLineIndex = findLastLineWithFont(tooltipLines, PAGE_FONT);
        int searchLimit = pageLineIndex >= 0 ? pageLineIndex : tooltipLines.size();
        int lastDividerLine = findLastDividerBefore(tooltipLines, searchLimit);

        if (lastDividerLine < 0) {
            return null;
        }

        GearInstance headerInstance = createSyntheticHeaderInstance(itemProperty);
        Component originalShinyRerollTracker = extractOriginalShinyRerollTracker(tooltipLines);
        List<Component> header = buildHeaderWithSyntheticRequirements(
                tooltipLines, itemProperty.getItemInfo(), headerInstance, lastDividerLine, originalShinyRerollTracker);
        int footerStartIndex = findUnidentifiedFooterStart(tooltipLines, lastDividerLine, pageLineIndex);
        List<Component> footer = footerStartIndex >= 0
                ? copyMarkedRange(
                        tooltipLines,
                        footerStartIndex,
                        tooltipLines.size(),
                        lineIndex -> classifyFooterMarker(tooltipLines.get(lineIndex)))
                : List.of();
        return new IdentifiableTooltipComponent.TooltipParts(header, footer);
    }

    private List<Component> buildHeaderWithSyntheticRequirements(
            List<Component> tooltipLines,
            GearInfo gearInfo,
            GearInstance gearInstance,
            int identificationDividerLine,
            Component originalShinyRerollTracker) {
        if (identificationDividerLine < 0) {
            List<Component> header = copyMarkedRange(tooltipLines, 0, tooltipLines.size(), lineIndex -> null);
            removeVanillaHoverNameLine(header, gearInfo);
            appendOverallPercentageToTitleLine(header, gearInfo, gearInstance);
            return header;
        }

        int requirementsStartLine = findRequirementsSectionStart(tooltipLines, identificationDividerLine);
        Map<Skill, Boolean> parsedSkillRequirementStates =
                parseSkillRequirementStates(tooltipLines, requirementsStartLine, identificationDividerLine);
        List<Component> header = copyMarkedRange(tooltipLines, 0, requirementsStartLine, lineIndex -> null);
        removeVanillaHoverNameLine(header, gearInfo);
        appendOverallPercentageToTitleLine(header, gearInfo, gearInstance);
        header.addAll(requirementsComponent.buildHeaderLines(
                gearInfo, gearInstance, parsedSkillRequirementStates, originalShinyRerollTracker));
        return header;
    }

    private static void removeVanillaHoverNameLine(List<Component> header, GearInfo gearInfo) {
        if (header.size() < 2) {
            return;
        }

        Component firstLine = header.get(0);
        Component secondLine = header.get(1);
        if (!containsFont(StyledText.fromComponent(firstLine), GearTooltipSupport.EMBLEM_FRAME_FONT)
                && firstLine.getString().trim().equals(gearInfo.name())
                && containsFont(StyledText.fromComponent(secondLine), GearTooltipSupport.EMBLEM_FRAME_FONT)) {
            header.remove(0);
        }
    }

    private void appendOverallPercentageToTitleLine(
            List<Component> header, GearInfo gearInfo, GearInstance gearInstance) {
        if (gearInstance == null || !gearInstance.hasOverallValue()) {
            return;
        }

        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        boolean perfectTitle = feature.perfect.get() && gearInstance.isPerfect();
        boolean defectiveTitle = feature.defective.get() && gearInstance.isDefective();
        if (!GearTitleComponent.shouldAppendOverallPercentage(gearInstance, feature)
                && !perfectTitle
                && !defectiveTitle) {
            return;
        }

        for (int i = 0; i < header.size(); i++) {
            Component line = header.get(i);
            StyledText styledText = StyledText.fromComponent(line);
            String visibleText = styledText.getNormalized().getString().trim();
            boolean isEmblemTitleLine = containsFont(styledText, GearTooltipSupport.EMBLEM_FRAME_FONT);

            if (visibleText.isBlank()) {
                continue;
            }

            if (!isEmblemTitleLine && !visibleText.endsWith(gearInfo.name())) {
                continue;
            }

            if (perfectTitle) {
                header.set(i, titleComponent.buildNameLine(gearInfo, gearInstance, true));
                return;
            }
            if (defectiveTitle) {
                header.set(i, titleComponent.buildNameLine(gearInfo, gearInstance, true));
                return;
            }

            MutableComponent updatedLine = line.copy();
            updatedLine.append(ColorScaleUtils.getPercentageTextComponent(
                            feature.getColorMap(),
                            gearInstance.getOverallPercentage(),
                            feature.colorLerp.get(),
                            feature.decimalPlaces.get())
                    .withStyle(style -> style.withFont(TooltipStyleSupport.WYNNCRAFT_LANGUAGE_FONT)));
            header.set(i, updatedLine);
            return;
        }
    }

    private static TooltipMarkers classifyFooterMarker(Component line) {
        StyledText styledText = StyledText.fromComponent(line);
        if (containsFont(styledText, PAGE_FONT)) {
            return TooltipMarkers.ALIGN_CENTER;
        }

        return null;
    }

    private static int findLastDividerBefore(List<Component> tooltipLines, int endExclusive) {
        for (int i = endExclusive - 1; i >= 0; i--) {
            if (containsFont(StyledText.fromComponent(tooltipLines.get(i)), DIVIDER_FONT)) {
                return i;
            }
        }

        return -1;
    }

    private static int findLastLineWithFont(List<Component> tooltipLines, FontDescription font) {
        for (int i = tooltipLines.size() - 1; i >= 0; i--) {
            if (containsFont(StyledText.fromComponent(tooltipLines.get(i)), font)) {
                return i;
            }
        }

        return -1;
    }

    private static int findRequirementsSectionStart(List<Component> tooltipLines, int identificationDividerLine) {
        int scanIndex = identificationDividerLine - 1;
        while (scanIndex >= 0) {
            Component line = tooltipLines.get(scanIndex);
            if (isDividerLine(line) || line.getString().isBlank() || isRequirementRelatedLine(line)) {
                scanIndex--;
                continue;
            }

            if (!line.getString().isBlank() && !isRequirementRelatedLine(line)) {
                break;
            }
        }

        return Math.max(0, scanIndex + 1);
    }

    private static Map<Skill, Boolean> parseSkillRequirementStates(
            List<Component> tooltipLines, int requirementsStartLine, int identificationDividerLine) {
        Map<Skill, Boolean> parsedStates = new EnumMap<>(Skill.class);

        for (int i = requirementsStartLine; i < identificationDividerLine; i++) {
            StyledText normalized =
                    StyledText.fromComponent(tooltipLines.get(i)).getNormalized();
            Matcher skillMatcher = normalized.getMatcher(SKILL_REQ_PATTERN);
            if (!skillMatcher.matches()) {
                continue;
            }

            Matcher partMatcher = normalized.getMatcher(SKILL_REQ_PART_PATTERN);
            int index = 0;
            while (partMatcher.find() && index < Skill.values().length) {
                parsedStates.put(Skill.values()[index], "\uE006".equals(partMatcher.group(1)));
                index++;
            }
            break;
        }

        return parsedStates;
    }

    private static GearInstance createSyntheticHeaderInstance(
            IdentifiableItemProperty<GearInfo, GearInstance> itemProperty) {
        if (!(itemProperty instanceof ShinyItemProperty shinyItemProperty)
                || shinyItemProperty.getShinyStat().isEmpty()) {
            return null;
        }

        return new GearInstance(
                List.of(), List.of(), 0, Optional.empty(), shinyItemProperty.getShinyStat(), false, Optional.empty());
    }

    private static int findUnidentifiedFooterStart(
            List<Component> tooltipLines, int lastDividerLine, int pageLineIndex) {
        if (pageLineIndex < 0) {
            return -1;
        }

        if (lastDividerLine < 0 || lastDividerLine >= pageLineIndex) {
            return pageLineIndex;
        }

        for (int i = lastDividerLine + 1; i < pageLineIndex; i++) {
            if (!containsFont(StyledText.fromComponent(tooltipLines.get(i)), IDENTIFICATION_MAJOR_FONT)) {
                continue;
            }

            int footerDividerLine = findLastDividerInRange(tooltipLines, lastDividerLine + 1, i);
            return footerDividerLine >= 0 ? footerDividerLine : i;
        }

        return pageLineIndex;
    }

    private static int findLastDividerInRange(List<Component> tooltipLines, int startInclusive, int endExclusive) {
        for (int i = endExclusive - 1; i >= startInclusive; i--) {
            if (containsFont(StyledText.fromComponent(tooltipLines.get(i)), DIVIDER_FONT)) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isRequirementRelatedLine(Component line) {
        StyledText styledText = StyledText.fromComponent(line);
        StyledText normalized = styledText.getNormalized();
        return containsFont(styledText, REQUIREMENT_FRAME_FONT)
                || containsFont(styledText, REQUIREMENT_SPRITE_FONT)
                || containsFont(styledText, GearTooltipSupport.COMMON_FONT)
                || normalized.getString().contains(SHINY_STAT_ICON);
    }

    private static Component extractOriginalShinyRerollTracker(List<Component> tooltipLines) {
        for (Component tooltipLine : tooltipLines) {
            StyledText styledLine = StyledText.fromComponent(tooltipLine);
            if (!styledLine.getNormalized().getString().contains(SHINY_STAT_ICON)) {
                continue;
            }

            Matcher matcher = styledLine.getMatcher(SHINY_STAT_PATTERN);
            if (matcher.matches()) {
                try {
                    return styledLine
                            .substring(matcher.start(3), StyleType.DEFAULT)
                            .trim()
                            .getComponent();
                } catch (RuntimeException ignored) {
                    // Fall through to the next line if the substring cannot be reconstructed safely.
                }
            }
        }

        return null;
    }

    private static boolean isDividerLine(Component line) {
        return containsFont(StyledText.fromComponent(line), DIVIDER_FONT);
    }

    private static int countFontParts(StyledText styledText, FontDescription font) {
        int count = 0;
        for (StyledTextPart part : styledText) {
            if (font.equals(part.getPartStyle().getFont())) {
                count++;
            }
        }

        return count;
    }

    private static List<Component> copyMarkedRange(
            List<Component> lines,
            int startInclusive,
            int endExclusive,
            java.util.function.IntFunction<TooltipMarkers> markerResolver) {
        List<Component> copy = new ArrayList<>(Math.max(0, endExclusive - startInclusive));
        for (int i = startInclusive; i < endExclusive; i++) {
            Component line = lines.get(i).copy();
            TooltipMarkers marker = markerResolver.apply(i);
            if (marker != null) {
                MutableComponent markedLine = line.copy();
                markedLine.setStyle(markedLine.getStyle().withInsertion(marker.token()));
                copy.add(markedLine);
            } else {
                copy.add(line);
            }
        }
        return copy;
    }
}

/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CraftedGearTooltipComponent extends CraftedTooltipComponent<CraftedGearItem> {
    private static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));
    private static final FontDescription TOOLTIP_DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));
    private static final FontDescription TOOLTIP_PAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/page"));
    private static final Style WYNNCRAFT_WHITE_STYLE = Style.EMPTY
            .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
            .withColor(ChatFormatting.WHITE);

    @Override
    public TooltipParts buildTooltipParts(ItemStack itemStack, CraftedGearItem craftedItem) {
        List<Component> tooltipLines = LoreUtils.getTooltipLines(itemStack);
        if (tooltipLines.isEmpty()) {
            return null;
        }

        int pageLineIndex = TooltipUtils.findFirstLineWithFont(tooltipLines, TOOLTIP_PAGE_FONT);
        int contentEnd = pageLineIndex >= 0 ? pageLineIndex : tooltipLines.size();
        int identificationDividerLine = findLastDividerBefore(tooltipLines, contentEnd);
        if (identificationDividerLine < 0) {
            return null;
        }

        int firstIdentificationLine = identificationDividerLine + 1;
        while (firstIdentificationLine < contentEnd
                && tooltipLines.get(firstIdentificationLine).getString().isBlank()) {
            firstIdentificationLine++;
        }

        if (firstIdentificationLine >= contentEnd) {
            return null;
        }

        List<Component> header = copyMarkedRange(
                tooltipLines, 0, firstIdentificationLine, craftedItem.getRequirements(), this::classifyHeaderMarker);
        removeDuplicateHoverNameLine(header, craftedItem.getName());

        List<Component> footer = pageLineIndex >= 0
                ? copyMarkedRange(
                        tooltipLines,
                        pageLineIndex,
                        tooltipLines.size(),
                        craftedItem.getRequirements(),
                        line -> TooltipUtils.containsFont(line, TOOLTIP_PAGE_FONT) ? TooltipMarkers.ALIGN_CENTER : null)
                : List.of();
        return new TooltipParts(header, footer);
    }

    @Override
    public List<Component> buildHeaderTooltip(CraftedGearItem craftedItem) {
        return List.of(Component.literal(craftedItem.getName()).withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public List<Component> buildFooterTooltip(CraftedGearItem craftedItem) {
        return List.of();
    }

    @Override
    public List<Component> finalizeTooltipLines(List<Component> tooltip, int targetWidth, CraftedGearItem craftedItem) {
        List<Component> finalized = new ArrayList<>(tooltip);
        GearTooltipAlignmentComponent.realignMarkedTooltipLines(finalized);
        return finalized;
    }

    private void removeDuplicateHoverNameLine(List<Component> header, String itemName) {
        if (header.size() < 2) {
            return;
        }

        int emblemTitleIndex = TooltipUtils.findFirstLineWithFont(header, EMBLEM_FRAME_FONT);
        if (emblemTitleIndex > 0
                && header.getFirst().getString().trim().equals(itemName)
                && TooltipUtils.containsFont(header.get(emblemTitleIndex), EMBLEM_FRAME_FONT)) {
            header.removeFirst();
        }
    }

    private TooltipMarkers classifyHeaderMarker(Component line) {
        if (TooltipUtils.containsFont(line, TOOLTIP_DIVIDER_FONT)) {
            return TooltipMarkers.SECTION_DIVIDER;
        }

        if (isRequirementValueLine(line)) {
            return TooltipMarkers.ALIGN_RIGHT;
        }

        return null;
    }

    private boolean isRequirementValueLine(Component line) {
        if (!TooltipUtils.containsFont(line, IdentifiableTooltipComponent.REQUIREMENT_STYLE.getFont())) {
            return false;
        }

        String lineText = line.getString();
        return lineText.contains("Combat Level") || lineText.contains("Class Type") || lineText.contains("Quest");
    }

    private List<Component> copyMarkedRange(
            List<Component> lines,
            int startInclusive,
            int endExclusive,
            GearRequirements requirements,
            java.util.function.Function<Component, TooltipMarkers> markerResolver) {
        List<Component> copy = new ArrayList<>(Math.max(0, endExclusive - startInclusive));
        for (int i = startInclusive; i < endExclusive; i++) {
            Component line = lines.get(i);
            TooltipMarkers marker = markerResolver.apply(line);
            if (marker == TooltipMarkers.ALIGN_RIGHT) {
                copy.add(TooltipMarkers.markLine(
                        rebuildRequirementLine(line, requirements), TooltipMarkers.ALIGN_RIGHT));
                continue;
            }

            copy.add(marker == null ? line.copy() : TooltipMarkers.markLine(line.copy(), marker));
        }

        return copy;
    }

    private MutableComponent rebuildRequirementLine(Component originalLine, GearRequirements requirements) {
        String lineText = originalLine.getString();
        if (lineText.contains("Combat Level") && requirements.level() != 0) {
            return buildRequirementValueLine(
                    Component.literal(" Combat Level").withStyle(WYNNCRAFT_WHITE_STYLE),
                    Component.literal(String.valueOf(requirements.level()))
                            .withStyle(Style.EMPTY
                                    .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                                    .withColor(ChatFormatting.GRAY)),
                    Models.CharacterStats.getLevel() >= requirements.level());
        }

        if (lineText.contains("Class Type") && requirements.classType().isPresent()) {
            ClassType classType = requirements.classType().get();
            return buildRequirementValueLine(
                    Component.literal(" Class Type").withStyle(WYNNCRAFT_WHITE_STYLE),
                    Component.literal(classType.getFullName())
                            .withStyle(Style.EMPTY
                                    .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                                    .withColor(ChatFormatting.GRAY)),
                    Models.Character.getClassType() == classType);
        }

        if (lineText.contains("Quest") && requirements.quest().isPresent()) {
            String questName = requirements.quest().get();
            Optional<QuestInfo> questInfo = Models.Quest.getQuestFromName(questName);
            int questLevel = questInfo.map(QuestInfo::level).orElse(1);
            boolean fulfilled = questInfo
                    .map(info -> info.status() == ActivityStatus.COMPLETED)
                    .orElse(false);

            MutableComponent value = Component.literal(StringUtils.shorten(questName, 10) + " ")
                    .withStyle(Style.EMPTY
                            .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.GRAY));
            value.append(Component.literal("(Lv. " + questLevel + ")")
                    .withStyle(Style.EMPTY
                            .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(ChatFormatting.DARK_GRAY)));

            return buildRequirementValueLine(
                    Component.literal(" Quest").withStyle(WYNNCRAFT_WHITE_STYLE), value, fulfilled);
        }

        return originalLine.copy();
    }

    private MutableComponent buildRequirementValueLine(Component label, Component value, boolean fulfilled) {
        MutableComponent requirement = Component.empty();
        requirement.append(withWhiteShadow(
                fulfilled
                        ? Component.literal("\uE006\uDAFF\uDFFF")
                                .withStyle(IdentifiableTooltipComponent.REQUIREMENT_STYLE)
                        : Component.literal("\uE007\uDAFF\uDFFF")
                                .withStyle(IdentifiableTooltipComponent.REQUIREMENT_STYLE)));
        requirement.append(label.copy());

        MutableComponent paddedValue = Component.literal("  ").withStyle(value.getStyle());
        paddedValue.append(value.copy());
        requirement.append(paddedValue);
        return requirement;
    }

    private MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }

    private int findLastDividerBefore(List<Component> tooltipLines, int endExclusive) {
        for (int i = endExclusive - 1; i >= 0; i--) {
            if (TooltipUtils.containsFont(tooltipLines.get(i), TOOLTIP_DIVIDER_FONT)) {
                return i;
            }
        }

        return -1;
    }
}

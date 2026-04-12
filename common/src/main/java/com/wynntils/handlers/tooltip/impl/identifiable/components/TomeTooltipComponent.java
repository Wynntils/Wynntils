/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.DividerComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.rewards.type.TomeRequirements;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class TomeTooltipComponent extends IdentifiableTooltipComponent<TomeInfo, TomeInstance> {
    private static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));
    private final RewardTitleComponent titleComponent = new RewardTitleComponent();
    private final DividerComponent dividerComponent = new DividerComponent();

    @Override
    public TooltipParts buildTooltipParts(
            ItemStack itemStack,
            IdentifiableItemProperty<TomeInfo, TomeInstance> itemProperty,
            boolean hideUnidentified,
            boolean showItemType) {
        List<Component> tooltipLines = LoreUtils.getTooltipLines(itemStack);
        if (tooltipLines.isEmpty()) {
            return null;
        }

        int contentEnd = tooltipLines.size();
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
                tooltipLines,
                0,
                firstIdentificationLine,
                itemProperty.getItemInfo().requirements().level(),
                line -> classifyHeaderMarker(
                        line, itemProperty.getItemInfo().requirements().level()));
        updateParsedTitleLine(
                header,
                itemProperty.getItemInfo(),
                itemProperty.getItemInstance().orElse(null));
        List<Component> footer = List.of();
        return new TooltipParts(header, footer);
    }

    @Override
    public List<Component> buildHeaderTooltip(TomeInfo tomeInfo, TomeInstance tomeInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

        MutableComponent itemName = buildRewardItemNameComponent(
                tomeInfo.name(),
                tomeInfo.tier().getChatFormatting(),
                tomeInstance != null && tomeInstance.isPerfect(),
                tomeInstance != null && tomeInstance.isDefective(),
                tomeInstance != null && tomeInstance.hasOverallValue(),
                tomeInstance != null ? tomeInstance.getOverallPercentage() : 0f);
        header.add(
                titleComponent.buildNameLine(GearType.MASTERY_TOME, itemName, tomeInstance == null, hideUnidentified));
        header.add(titleComponent.buildTagsLine(
                tomeInfo.tier(), "Tome", tomeInfo.metaInfo().restrictions()));

        TomeRequirements requirements = tomeInfo.requirements();
        int level = requirements.level();
        if (level != 0) {
            header.add(TooltipMarkers.markLine(
                    dividerComponent.buildDivider(tomeInfo.tier()).copy(), TooltipMarkers.SECTION_DIVIDER));
            header.add(TooltipMarkers.markLine(buildCombatLevelRequirementLine(level), TooltipMarkers.ALIGN_RIGHT));
        }

        header.add(TooltipMarkers.markLine(
                dividerComponent.buildDivider(tomeInfo.tier()).copy(), TooltipMarkers.IDENTIFICATION_DIVIDER));

        return header;
    }

    @Override
    public List<Component> buildFooterTooltip(TomeInfo tomeInfo, TomeInstance tomeInstance, boolean showItemType) {
        return List.of();
    }

    @Override
    public List<Component> finalizeTooltipLines(
            List<Component> tooltip, int targetWidth, TomeInfo tomeInfo, TomeInstance tomeInstance) {
        List<Component> finalized = new ArrayList<>(tooltip);
        GearTooltipAlignmentComponent.realignMarkedTooltipLines(finalized);
        return finalized;
    }

    private void updateParsedTitleLine(List<Component> header, TomeInfo tomeInfo, TomeInstance tomeInstance) {
        if (header.isEmpty()) {
            return;
        }

        int emblemTitleIndex = TooltipUtils.findFirstLineWithFont(header, EMBLEM_FRAME_FONT);
        if (emblemTitleIndex > 0
                && header.getFirst().getString().trim().equals(tomeInfo.name())
                && TooltipUtils.containsFont(header.get(emblemTitleIndex), EMBLEM_FRAME_FONT)) {
            header.removeFirst();
            emblemTitleIndex--;
        }

        int titleLineIndex = emblemTitleIndex >= 0 ? emblemTitleIndex : TooltipUtils.findFirstNonBlankLine(header);
        if (titleLineIndex < 0) {
            return;
        }

        MutableComponent updatedTitleLine = header.get(titleLineIndex).copy();
        MutableComponent styledName = buildRewardItemNameComponent(
                tomeInfo.name(),
                tomeInfo.tier().getChatFormatting(),
                tomeInstance != null && tomeInstance.isPerfect(),
                tomeInstance != null && tomeInstance.isDefective(),
                tomeInstance != null && tomeInstance.hasOverallValue(),
                tomeInstance != null ? tomeInstance.getOverallPercentage() : 0f);
        if (!TooltipUtils.replaceTrailingTitleComponent(updatedTitleLine, tomeInfo.name(), styledName)) {
            updatedTitleLine = styledName;
        }

        header.set(titleLineIndex, updatedTitleLine);
    }

    private MutableComponent buildCombatLevelRequirementLine(int levelRequirement) {
        boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= levelRequirement;
        Component label = Component.literal(" Combat Level").withStyle(WYNNCRAFT_WHITE_STYLE);
        Component value = Component.literal(String.valueOf(levelRequirement))
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY));
        return buildRequirementValueLine(label, value, fulfilled);
    }

    private TooltipMarkers classifyHeaderMarker(Component line, int levelRequirement) {
        if (TooltipUtils.containsFont(line, TOOLTIP_DIVIDER_FONT)) {
            return TooltipMarkers.SECTION_DIVIDER;
        }

        if (levelRequirement != 0 && isCombatLevelRequirementLine(line)) {
            return TooltipMarkers.ALIGN_RIGHT;
        }

        return null;
    }

    private boolean isCombatLevelRequirementLine(Component line) {
        return TooltipUtils.containsFont(line, REQUIREMENT_STYLE.getFont())
                && line.getString().contains("Combat Level");
    }

    private List<Component> copyMarkedRange(
            List<Component> lines,
            int startInclusive,
            int endExclusive,
            int levelRequirement,
            java.util.function.Function<Component, TooltipMarkers> markerResolver) {
        List<Component> copy = new ArrayList<>(Math.max(0, endExclusive - startInclusive));
        for (int i = startInclusive; i < endExclusive; i++) {
            Component line = lines.get(i);
            TooltipMarkers marker = markerResolver.apply(line);
            if (marker == TooltipMarkers.ALIGN_RIGHT) {
                copy.add(TooltipMarkers.markLine(
                        buildCombatLevelRequirementLine(levelRequirement), TooltipMarkers.ALIGN_RIGHT));
                continue;
            }

            copy.add(marker == null ? line.copy() : TooltipMarkers.markLine(line.copy(), marker));
        }

        return copy;
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

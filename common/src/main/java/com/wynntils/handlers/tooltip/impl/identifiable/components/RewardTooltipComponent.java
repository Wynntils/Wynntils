/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.DividerComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
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

public abstract class RewardTooltipComponent<T, I> extends IdentifiableTooltipComponent<T, I> {
    private static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));
    private final RewardTitleComponent titleComponent = new RewardTitleComponent();
    private final DividerComponent dividerComponent = new DividerComponent();

    @Override
    public TooltipParts buildTooltipParts(
            ItemStack itemStack,
            IdentifiableItemProperty<T, I> itemProperty,
            boolean hideUnidentified,
            boolean showItemType) {
        List<Component> tooltipLines = LoreUtils.getTooltipLines(itemStack);
        if (tooltipLines.isEmpty()) {
            return null;
        }

        int contentEnd = getContentEnd(tooltipLines);
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

        T itemInfo = itemProperty.getItemInfo();
        I itemInstance = itemProperty.getItemInstance().orElse(null);
        int levelRequirement = getLevelRequirement(itemInfo);

        List<Component> header = copyMarkedRange(
                tooltipLines,
                0,
                firstIdentificationLine,
                levelRequirement,
                line -> classifyHeaderMarker(line, levelRequirement));
        updateParsedTitleLine(header, itemInfo, itemInstance);

        List<Component> footer = contentEnd < tooltipLines.size()
                ? copyMarkedRange(
                        tooltipLines,
                        contentEnd,
                        tooltipLines.size(),
                        0,
                        line -> TooltipUtils.containsFont(line, TOOLTIP_PAGE_FONT) ? TooltipMarkers.ALIGN_CENTER : null)
                : List.of();
        return new TooltipParts(header, footer);
    }

    @Override
    public List<Component> buildHeaderTooltip(T itemInfo, I itemInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();
        GearTier tier = getTier(itemInfo);
        GearRestrictions restrictions = getRestrictions(itemInfo);
        int levelRequirement = getLevelRequirement(itemInfo);

        MutableComponent itemName = buildRewardItemNameComponent(
                getItemName(itemInfo),
                tier.getChatFormatting(),
                itemInstance != null && isPerfect(itemInstance),
                itemInstance != null && isDefective(itemInstance),
                itemInstance != null && hasOverallValue(itemInstance),
                itemInstance != null ? getOverallPercentage(itemInstance) : 0f);
        header.add(titleComponent.buildNameLine(getGearType(), itemName, itemInstance == null, hideUnidentified));
        header.add(titleComponent.buildTagsLine(tier, getTypeName(), restrictions));

        if (levelRequirement != 0) {
            header.add(TooltipMarkers.markLine(
                    dividerComponent.buildDivider(tier).copy(), TooltipMarkers.SECTION_DIVIDER));
            header.add(TooltipMarkers.markLine(
                    buildCombatLevelRequirementLine(levelRequirement), TooltipMarkers.ALIGN_RIGHT));
        }

        header.add(TooltipMarkers.markLine(
                dividerComponent.buildDivider(tier).copy(), TooltipMarkers.IDENTIFICATION_DIVIDER));
        return header;
    }

    @Override
    public List<Component> buildFooterTooltip(T itemInfo, I itemInstance, boolean showItemType) {
        return List.of();
    }

    @Override
    public List<Component> finalizeTooltipLines(List<Component> tooltip, int targetWidth, T itemInfo, I itemInstance) {
        List<Component> finalized = new ArrayList<>(tooltip);
        GearTooltipAlignmentComponent.realignMarkedTooltipLines(finalized);
        return finalized;
    }

    protected int getContentEnd(List<Component> tooltipLines) {
        return tooltipLines.size();
    }

    protected abstract GearType getGearType();

    protected abstract String getTypeName();

    protected abstract String getItemName(T itemInfo);

    protected abstract GearTier getTier(T itemInfo);

    protected abstract GearRestrictions getRestrictions(T itemInfo);

    protected abstract int getLevelRequirement(T itemInfo);

    protected abstract boolean isPerfect(I itemInstance);

    protected abstract boolean isDefective(I itemInstance);

    protected abstract boolean hasOverallValue(I itemInstance);

    protected abstract float getOverallPercentage(I itemInstance);

    private void updateParsedTitleLine(List<Component> header, T itemInfo, I itemInstance) {
        if (header.isEmpty()) {
            return;
        }

        String itemName = getItemName(itemInfo);
        int emblemTitleIndex = TooltipUtils.findFirstLineWithFont(header, EMBLEM_FRAME_FONT);
        if (emblemTitleIndex > 0
                && header.getFirst().getString().trim().equals(itemName)
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
                itemName,
                getTier(itemInfo).getChatFormatting(),
                itemInstance != null && isPerfect(itemInstance),
                itemInstance != null && isDefective(itemInstance),
                itemInstance != null && hasOverallValue(itemInstance),
                itemInstance != null ? getOverallPercentage(itemInstance) : 0f);
        if (!TooltipUtils.replaceTrailingTitleComponent(updatedTitleLine, itemName, styledName)) {
            updatedTitleLine = styledName;
        }

        header.set(titleLineIndex, updatedTitleLine);
    }

    private MutableComponent buildCombatLevelRequirementLine(int levelRequirement) {
        Component label = Component.literal(" Combat Level").withStyle(WYNNCRAFT_WHITE_STYLE);
        Component value = Component.literal(String.valueOf(levelRequirement))
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY));
        return buildRequirementValueLine(
                label, value, Models.CombatXp.getCombatLevel().current() >= levelRequirement);
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

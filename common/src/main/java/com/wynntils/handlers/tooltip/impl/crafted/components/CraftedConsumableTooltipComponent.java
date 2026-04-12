/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted.components;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTooltipAlignmentComponent;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CraftedConsumableTooltipComponent extends CraftedTooltipComponent<CraftedConsumableItem> {
    private static final FontDescription EMBLEM_FRAME_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame"));

    @Override
    public TooltipParts buildTooltipParts(ItemStack itemStack, CraftedConsumableItem craftedItem) {
        List<Component> tooltipLines = LoreUtils.getTooltipLines(itemStack);
        if (tooltipLines.isEmpty()) {
            return null;
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

        List<Component> header = copyRange(
                tooltipLines, 0, firstIdentificationLine >= 0 ? firstIdentificationLine : tooltipLines.size());
        removeDuplicateHoverNameLine(header, craftedItem);

        if (firstIdentificationLine < 0 || lastIdentificationLine < firstIdentificationLine) {
            return new TooltipParts(header, List.of());
        }

        List<Component> footer = copyRange(tooltipLines, lastIdentificationLine + 1, tooltipLines.size());
        return new TooltipParts(header, footer);
    }

    @Override
    public List<Component> buildHeaderTooltip(CraftedConsumableItem craftedItem) {
        List<Component> header = new ArrayList<>();

        // name
        header.add(Component.literal(craftedItem.getName())
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(" [" + craftedItem.getUses().current() + "/"
                                + craftedItem.getUses().max() + "]")
                        .withStyle(ChatFormatting.AQUA)));

        // Effects
        if (!craftedItem.getNamedEffects().isEmpty()) {
            header.add(Component.literal("Effect:").withStyle(ChatFormatting.GREEN));
            craftedItem
                    .getNamedEffects()
                    .forEach(effect -> header.add(Component.literal("- ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                            StringUtils.capitalizeFirst(
                                                            effect.type().name().toLowerCase(Locale.ROOT)) + ": ")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(String.valueOf(effect.value()))
                                    .withStyle(ChatFormatting.WHITE)
                                    .append(Component.literal(
                                            " " + effect.type().getSuffix())))));
            header.add(Component.literal(""));
        }

        // requirements
        int level = craftedItem.getLevel();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            header.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            header.add(Component.literal(""));
        }

        return header;
    }

    @Override
    public List<Component> buildFooterTooltip(CraftedConsumableItem craftedItem) {
        List<Component> footer = new ArrayList<>();

        footer.add(Component.empty());

        // item type
        footer.add(Component.literal("Crafted ")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(StringUtils.capitalizeFirst(
                        craftedItem.getConsumableType().name().toLowerCase(Locale.ROOT)))));

        return footer;
    }

    @Override
    public List<Component> finalizeTooltipLines(
            List<Component> tooltip, int targetWidth, CraftedConsumableItem craftedItem) {
        List<Component> finalized = new ArrayList<>(tooltip);
        GearTooltipAlignmentComponent.realignMarkedTooltipLines(finalized);
        return finalized;
    }

    private boolean isIdentificationLine(Component line) {
        StyledText normalized = StyledText.fromComponent(line).getNormalized();
        return normalized.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN).matches();
    }

    private void removeDuplicateHoverNameLine(List<Component> header, CraftedConsumableItem craftedItem) {
        if (header.size() < 2) {
            return;
        }

        String firstLineText = header.getFirst().getString().trim();
        if (!TooltipUtils.containsFont(header.get(1), EMBLEM_FRAME_FONT)) {
            return;
        }

        if (firstLineText.equals(craftedItem.getName()) || firstLineText.startsWith(craftedItem.getName() + " [")) {
            header.removeFirst();
        }
    }

    private List<Component> copyRange(List<Component> lines, int startInclusive, int endExclusive) {
        List<Component> copy = new ArrayList<>(Math.max(0, endExclusive - startInclusive));
        for (int i = startInclusive; i < endExclusive; i++) {
            copy.add(lines.get(i).copy());
        }
        return copy;
    }
}

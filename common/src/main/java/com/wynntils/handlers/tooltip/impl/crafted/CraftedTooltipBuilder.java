/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private final CraftedItemProperty craftedItem;
    private final List<Component> sourceLines;
    private final boolean synthetic;
    private final Map<TooltipKey, List<Component>> cache = new HashMap<>();

    private CraftedTooltipBuilder(
            CraftedItemProperty craftedItem,
            List<Component> header,
            List<Component> footer,
            List<Component> sourceLines,
            String source,
            boolean synthetic) {
        super(header, footer, source);
        this.craftedItem = craftedItem;
        this.sourceLines = List.copyOf(sourceLines);
        this.synthetic = synthetic;
    }

    public static CraftedTooltipBuilder buildNewItem(CraftedItemProperty craftedItem, String source) {
        Component title = Component.literal(craftedItem.getName())
                .withStyle(
                        craftedItem instanceof GearTierItemProperty tierItem
                                ? tierItem.getGearTier().getChatFormatting()
                                : ChatFormatting.WHITE);
        return new CraftedTooltipBuilder(craftedItem, List.of(title), List.of(), List.of(), source, true);
    }

    public static CraftedTooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItem) {
        return fromTooltipLines(LoreUtils.getTooltipLines(itemStack), craftedItem);
    }

    public static CraftedTooltipBuilder fromTooltipLines(
            List<Component> tooltipLines, CraftedItemProperty craftedItem) {
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltipLines);
        return new CraftedTooltipBuilder(craftedItem, splitLore.a(), splitLore.b(), tooltipLines, "", false);
    }

    @Override
    public List<Component> getTooltipLines(ClassType currentClass, TooltipOptions options) {
        if (craftedItem instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            if (!synthetic) return sourceLines;

            return buildTooltipLines(currentClass, options.style(), null);
        }

        int currentPage = craftedItem instanceof PagedItemProperty pagedItem ? pagedItem.currentPage() : 0;
        TooltipKey key = new TooltipKey(currentClass, options, currentPage);
        return cache.computeIfAbsent(
                key,
                ignored -> buildTooltipLines(
                        currentClass, options.style(), new CraftedTooltipOptionDecorator(craftedItem, options)));
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return craftedItem instanceof GearTierItemProperty tierItem
                ? tierItem.getGearTier().getChatFormatting()
                : ChatFormatting.WHITE;
    }

    @Override
    protected List<Component> decorateHeader(
            List<Component> header, TooltipIdentificationDecorator identificationDecorator) {
        if (identificationDecorator == null) return header;

        List<Component> decoratedHeader = new ArrayList<>(header);
        for (int i = 0; i < decoratedHeader.size(); i++) {
            Component originalLine = decoratedHeader.get(i);
            if (originalLine.getString().trim().equals(craftedItem.getName())) {
                decoratedHeader.set(i, identificationDecorator.getTitle(originalLine));
                return List.copyOf(decoratedHeader);
            }

            MutableComponent line = originalLine.copy();
            List<Component> siblings = line.getSiblings();
            for (int j = siblings.size() - 1; j >= 0; j--) {
                Component sibling = siblings.get(j);
                String text = sibling.getString().trim();
                if (!text.equals(craftedItem.getName()) && !text.endsWith(craftedItem.getName())) continue;

                siblings.set(j, identificationDecorator.getTitle(sibling));
                decoratedHeader.set(i, line);
                return List.copyOf(decoratedHeader);
            }
        }

        return List.copyOf(decoratedHeader);
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator, int targetWidth) {
        if (craftedItem instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            return List.of();
        }

        return CraftedTooltipIdentifications.buildTooltip(craftedItem, currentClass, decorator, style, targetWidth);
    }

    @Override
    protected List<Component> postProcessTooltipLines(List<Component> tooltip) {
        if (!(craftedItem instanceof CraftedGearItem craftedGear)) return List.copyOf(tooltip);

        List<AlignmentEntry> entries = tooltip.stream()
                .map(line -> classifyForAlignment(line, craftedGear))
                .toList();
        int targetWidth = entries.stream()
                .map(AlignmentEntry::component)
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);

        return entries.stream().map(entry -> alignEntry(entry, targetWidth)).toList();
    }

    private AlignmentEntry classifyForAlignment(Component line, CraftedGearItem item) {
        if (TooltipUtils.containsFont(line, CommonFonts.DIVIDER_FONT)) {
            return new AlignmentEntry(Alignment.CENTERED, stripLeadingOffsets(line));
        }
        if (TooltipUtils.containsFont(line, CommonFonts.PAGE_FONT)) {
            return new AlignmentEntry(Alignment.PAGINATOR, stripLeadingOffsets(line));
        }
        if (isRequirementValueLine(line)) {
            return new AlignmentEntry(Alignment.RIGHT, rebuildRequirementLine(line, item.getRequirements()));
        }
        return new AlignmentEntry(Alignment.FIXED, line);
    }

    private Component alignEntry(AlignmentEntry entry, int targetWidth) {
        return switch (entry.alignment()) {
            case FIXED -> entry.component();
            case CENTERED -> alignCentered(entry.component(), targetWidth);
            case PAGINATOR -> alignPaginator(entry.component(), targetWidth);
            case RIGHT -> alignRight(entry.component(), targetWidth);
        };
    }

    private boolean isRequirementValueLine(Component line) {
        if (!TooltipUtils.containsFont(line, CommonFonts.REQUIREMENT_SPRITE_FONT)) return false;

        String text = line.getString();
        return text.contains("Combat Level") || text.contains("Class Type") || text.contains("Quest");
    }

    private Component rebuildRequirementLine(Component originalLine, GearRequirements requirements) {
        String text = originalLine.getString();
        if (text.contains("Combat Level") && requirements.level() != 0) {
            return buildRequirementLine(
                    " Combat Level",
                    Component.literal(String.valueOf(requirements.level()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)),
                    Models.CharacterStats.getLevel() >= requirements.level());
        }

        if (text.contains("Class Type") && requirements.classType().isPresent()) {
            ClassType classType = requirements.classType().get();
            return buildRequirementLine(
                    " Class Type",
                    Component.literal(classType.getFullName())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)),
                    Models.Character.getClassType() == classType);
        }

        if (text.contains("Quest") && requirements.quest().isPresent()) {
            String questName = requirements.quest().get();
            Optional<QuestInfo> questInfo = Models.Quest.getQuestFromName(questName);
            int questLevel = questInfo.map(QuestInfo::level).orElse(1);
            boolean fulfilled = questInfo
                    .map(info -> info.status() == ActivityStatus.COMPLETED)
                    .orElse(false);
            Component value = Component.literal(StringUtils.shorten(questName, 10) + " ")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.GRAY))
                    .append(Component.literal("(Lv. " + questLevel + ")")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.DARK_GRAY)));
            return buildRequirementLine(" Quest", value, fulfilled);
        }

        return stripLeadingOffsets(originalLine);
    }

    private Component buildRequirementLine(String label, Component value, boolean fulfilled) {
        MutableComponent left = withWhiteShadow(Component.literal((fulfilled ? "\uE006" : "\uE007") + "\uDAFF\uDFFF")
                .withStyle(Style.EMPTY.withFont(CommonFonts.REQUIREMENT_SPRITE_FONT)));
        left.append(Component.literal(label).withStyle(CommonStyles.LANGUAGE));

        MutableComponent right = Component.literal("  ").withStyle(value.getStyle());
        right.append(value.copy());
        return Component.empty().append(left).append(right);
    }

    private Component stripLeadingOffsets(Component line) {
        List<Component> siblings = line.getSiblings();
        int firstContent = 0;
        while (firstContent < siblings.size() && isOffset(siblings.get(firstContent))) {
            firstContent++;
        }

        MutableComponent stripped = Component.empty().withStyle(line.getStyle());
        for (int i = firstContent; i < siblings.size(); i++) {
            stripped.append(siblings.get(i).copy());
        }
        return stripped;
    }

    private boolean isOffset(Component component) {
        return CommonFonts.SPACE_FONT.equals(component.getStyle().getFont())
                && component.getSiblings().isEmpty();
    }

    private Component alignCentered(Component line, int targetWidth) {
        int padding = Math.max(0, (targetWidth - McUtils.mc().font.width(line)) / 2);
        return withLeadingOffset(line, padding);
    }

    private Component alignPaginator(Component line, int targetWidth) {
        List<Component> leaves = flattenLeaves(line);
        int prefixWidth = 0;
        int paginatorWidth = 0;
        boolean foundPaginator = false;

        for (Component leaf : leaves) {
            if (!foundPaginator && CommonFonts.PAGE_FONT.equals(leaf.getStyle().getFont())) {
                foundPaginator = true;
            }
            if (foundPaginator) {
                paginatorWidth += McUtils.mc().font.width(leaf);
            } else {
                prefixWidth += McUtils.mc().font.width(leaf);
            }
        }

        if (!foundPaginator) return alignCentered(line, targetWidth);

        int padding = Math.max(0, ((targetWidth - paginatorWidth) / 2) - prefixWidth);
        return withLeadingOffset(line, padding);
    }

    private Component alignRight(Component line, int targetWidth) {
        List<Component> siblings = line.getSiblings();
        if (siblings.size() < 2) {
            int padding = Math.max(0, targetWidth - McUtils.mc().font.width(line));
            return withLeadingOffset(line, padding);
        }

        Component left = siblings.getFirst();
        Component right = siblings.getLast();
        MutableComponent aligned = Component.empty().append(left.copy());
        int width = McUtils.mc().font.width(left) + McUtils.mc().font.width(right);
        appendOffset(aligned, Math.max(0, targetWidth - width));
        aligned.append(right.copy());
        return aligned;
    }

    private Component withLeadingOffset(Component line, int pixels) {
        MutableComponent aligned = Component.empty();
        appendOffset(aligned, pixels);
        aligned.append(line.copy());
        return aligned;
    }

    private void appendOffset(MutableComponent line, int pixels) {
        if (pixels <= 0) return;

        String offset = Managers.Font.calculateOffset(0, pixels);
        if (!offset.isEmpty()) {
            line.append(Component.literal(offset).withStyle(CommonStyles.SPACE));
        }
    }

    private List<Component> flattenLeaves(Component component) {
        List<Component> leaves = new ArrayList<>();
        collectLeaves(component, Style.EMPTY, leaves);
        return leaves;
    }

    private void collectLeaves(Component component, Style inheritedStyle, List<Component> leaves) {
        Style effectiveStyle = component.getStyle().applyTo(inheritedStyle);
        MutableComponent leaf = component.copy();
        leaf.getSiblings().clear();
        leaf.setStyle(effectiveStyle);
        if (!leaf.getString().isEmpty()) leaves.add(leaf);

        for (Component sibling : component.getSiblings()) {
            collectLeaves(sibling, effectiveStyle, leaves);
        }
    }

    private MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }

    private enum Alignment {
        FIXED,
        CENTERED,
        PAGINATOR,
        RIGHT
    }

    private record AlignmentEntry(Alignment alignment, Component component) {}

    private record TooltipKey(ClassType currentClass, TooltipOptions options, int currentPage) {}
}

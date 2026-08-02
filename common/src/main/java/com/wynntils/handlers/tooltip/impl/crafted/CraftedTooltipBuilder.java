/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.TooltipLayout;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.gear.type.GearEmblem;
import com.wynntils.models.gear.type.GearEmblem.GearEmblemShape;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private static final int TOOLTIP_MIN_WIDTH = 140;
    private static final int CRAFTED_ACCENT_COLOR = 0xaed4d4;
    private static final int CRAFTED_EMBLEM_VARIANT = 4;
    private static final String CRAFTED_GEAR_FRAME =
            new GearEmblem(GearEmblemShape.STICKER, CRAFTED_EMBLEM_VARIANT).getFrameCode();
    private static final String CRAFTED_CONSUMABLE_FRAME =
            new GearEmblem(GearEmblemShape.HEXAGON, CRAFTED_EMBLEM_VARIANT).getFrameCode();
    private static final FontDescription IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));

    private final CraftedItemProperty craftedItem;
    private final List<Component> header;
    private final List<Component> footer;
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
        this.header = List.copyOf(header);
        this.footer = List.copyOf(footer);
        this.sourceLines = List.copyOf(sourceLines);
        this.synthetic = synthetic;
    }

    public static CraftedTooltipBuilder buildNewItem(CraftedItemProperty craftedItem, String source) {
        return new CraftedTooltipBuilder(craftedItem, List.of(), List.of(), List.of(), source, true);
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
        if (!synthetic && craftedItem instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            return sourceLines;
        }

        int currentPage = craftedItem instanceof PagedItemProperty pagedItem ? pagedItem.currentPage() : 0;
        TooltipKey key = new TooltipKey(currentClass, options, currentPage);
        return cache.computeIfAbsent(key, ignored -> buildCraftedTooltip(currentClass, options));
    }

    private List<Component> buildCraftedTooltip(ClassType currentClass, TooltipOptions options) {
        boolean statPage = !(craftedItem instanceof PagedItemProperty pagedItem) || pagedItem.isStatPage();
        CraftedTooltipOptionDecorator decorator =
                statPage ? new CraftedTooltipOptionDecorator(craftedItem, options) : null;

        if (synthetic) {
            return prependSourceWithSpacing(TooltipLayout.align(
                    buildSyntheticLines(currentClass, options.style(), decorator), TOOLTIP_MIN_WIDTH));
        }

        List<TooltipLine> lines = new ArrayList<>();
        decorateHeader(header, decorator).stream()
                .map(this::classifyForAlignment)
                .forEach(lines::add);
        if (statPage) {
            lines.addAll(
                    CraftedTooltipIdentifications.buildLines(craftedItem, currentClass, decorator, options.style()));
        }
        footer.stream().map(this::classifyForAlignment).forEach(lines::add);

        return prependSourceWithSpacing(TooltipLayout.align(lines));
    }

    private List<Component> prependSourceWithSpacing(List<Component> lines) {
        List<Component> withSource = prependSource(lines);
        if (withSource.size() == lines.size()) return withSource;

        List<Component> spacedLines = new ArrayList<>(withSource.size() + 1);
        spacedLines.add(withSource.getFirst());
        spacedLines.add(Component.empty());
        spacedLines.addAll(withSource.subList(1, withSource.size()));
        return List.copyOf(spacedLines);
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return craftedItem instanceof GearTierItemProperty tierItem
                ? tierItem.getGearTier().getChatFormatting()
                : ChatFormatting.WHITE;
    }

    @Override
    protected List<Component> decorateHeader(
            List<Component> originalHeader, TooltipIdentificationDecorator identificationDecorator) {
        if (identificationDecorator == null) return originalHeader;

        List<Component> decoratedHeader = new ArrayList<>(originalHeader);
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

    private TooltipLine classifyForAlignment(Component line) {
        if (!(craftedItem instanceof CraftedGearItem craftedGear)) {
            return new TooltipLine.Fixed(line);
        }
        if (TooltipUtils.containsFont(line, CommonFonts.WYNNTILS_TOOLTIP_DIVIDER_FONT)
                || TooltipUtils.containsFont(line, CommonFonts.TOOLTIP_PAGE_FONT)) {
            return new TooltipLine.Centered(stripLeadingOffsets(line));
        }
        if (isRequirementValueLine(line)) {
            return rebuildRequirementLine(line, craftedGear.getRequirements());
        }
        return new TooltipLine.Fixed(line);
    }

    private boolean isRequirementValueLine(Component line) {
        if (!TooltipUtils.containsFont(line, CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)) return false;

        String text = line.getString();
        return text.contains("Combat Level") || text.contains("Class Type") || text.contains("Quest");
    }

    private TooltipLine rebuildRequirementLine(Component originalLine, GearRequirements requirements) {
        String text = originalLine.getString();
        if (text.contains("Combat Level") && requirements.level() != 0) {
            return buildAlignedRequirementLine(
                    " Combat Level",
                    Component.literal(String.valueOf(requirements.level()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)),
                    Models.CharacterStats.getLevel() >= requirements.level());
        }

        if (text.contains("Class Type") && requirements.classType().isPresent()) {
            ClassType classType = requirements.classType().get();
            return buildAlignedRequirementLine(
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
            return buildAlignedRequirementLine(" Quest", value, fulfilled);
        }

        return new TooltipLine.Fixed(stripLeadingOffsets(originalLine));
    }

    private TooltipLine buildAlignedRequirementLine(String label, Component value, boolean fulfilled) {
        MutableComponent left = withWhiteShadow(Component.literal((fulfilled ? "\uE006" : "\uE007") + "\uDAFF\uDFFF")
                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)));
        left.append(Component.literal(label).withStyle(CommonStyles.LANGUAGE));
        return new TooltipLine.Aligned(left, value);
    }

    private Component stripLeadingOffsets(Component line) {
        List<Component> siblings = line.getSiblings();
        int firstContent = 0;
        while (firstContent < siblings.size() && isOffset(siblings.get(firstContent))) {
            firstContent++;
        }

        MutableComponent stripped = line.copy();
        stripped.getSiblings().clear();
        for (int i = firstContent; i < siblings.size(); i++) {
            stripped.append(siblings.get(i).copy());
        }
        return stripped;
    }

    private boolean isOffset(Component component) {
        return CommonFonts.SPACE_FONT.equals(component.getStyle().getFont())
                && component.getSiblings().isEmpty();
    }

    private List<TooltipLine> buildSyntheticLines(
            ClassType currentClass, TooltipStyle style, CraftedTooltipOptionDecorator decorator) {
        if (craftedItem instanceof CraftedGearItem gearItem) {
            return buildSyntheticGearLines(gearItem, currentClass, style, decorator);
        }
        if (craftedItem instanceof CraftedConsumableItem consumableItem) {
            return buildSyntheticConsumableLines(consumableItem, currentClass, style, decorator);
        }

        return List.of(new TooltipLine.Fixed(buildSyntheticTitle(craftedItem, decorator)));
    }

    private List<TooltipLine> buildSyntheticGearLines(
            CraftedGearItem item, ClassType currentClass, TooltipStyle style, CraftedTooltipOptionDecorator decorator) {
        List<TooltipLine> lines = new ArrayList<>();
        GearType type = item.getGearType();
        lines.add(new TooltipLine.Fixed(
                buildNameLine(CRAFTED_GEAR_FRAME, type.getFrameSpriteCode(), buildSyntheticTitle(item, decorator))));
        lines.add(new TooltipLine.Fixed(buildTypeLine(type.name())));
        lines.add(new TooltipLine.Fixed(Component.empty()));

        if (type.isWeapon()) {
            lines.add(new TooltipLine.Fixed(Component.literal(String.format(Locale.ROOT, "%,d", item.getDps()))
                    .withStyle(Style.EMPTY.withFont(CommonFonts.OFFSET_QUAD_12).withColor(CRAFTED_ACCENT_COLOR))
                    .append(Component.literal(" DPS").withStyle(CommonStyles.LANGUAGE))));
        } else {
            lines.add(new TooltipLine.Fixed(Component.literal(StringUtils.toSignedCommaString(item.getHealth()))
                    .withStyle(Style.EMPTY.withFont(CommonFonts.OFFSET_QUAD_12).withColor(CRAFTED_ACCENT_COLOR))
                    .append(Component.literal(" Health").withStyle(CommonStyles.LANGUAGE))));
        }

        item.getAttackSpeed()
                .ifPresent(speed -> lines.add(new TooltipLine.Fixed(Component.empty()
                        .append(withWhiteShadow(Component.literal("\uE007")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))))
                        .append(Component.literal(" " + speed.getName() + " ")
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.GRAY)))
                        .append(Component.literal("(" + speed.getHitsPerSecond() + " hits/s)")
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.DARK_GRAY))))));
        if (!item.getDamages().isEmpty()) {
            lines.add(new TooltipLine.Fixed(buildDamageLine(item.getDamages())));
        }
        if (!item.getDefences().isEmpty()) {
            lines.add(new TooltipLine.Fixed(buildDefenceLine(item.getDefences())));
        }
        lines.add(new TooltipLine.Fixed(buildDurabilityLine(item)));

        lines.add(new TooltipLine.Centered(divider()));
        lines.addAll(buildGearRequirements(item));
        lines.add(new TooltipLine.Centered(divider()));
        lines.addAll(CraftedTooltipIdentifications.buildLines(item, currentClass, decorator, style));
        lines.addAll(buildPaginator(item));
        return List.copyOf(lines);
    }

    private List<TooltipLine> buildSyntheticConsumableLines(
            CraftedConsumableItem item,
            ClassType currentClass,
            TooltipStyle style,
            CraftedTooltipOptionDecorator decorator) {
        List<TooltipLine> lines = new ArrayList<>();
        ConsumableType type = item.getConsumableType();
        lines.add(new TooltipLine.Fixed(buildNameLine(
                CRAFTED_CONSUMABLE_FRAME, type.getFrameSpriteCode(), buildSyntheticTitle(item, decorator))));
        lines.add(new TooltipLine.Fixed(buildTypeLine(type.name())));
        lines.add(new TooltipLine.Fixed(Component.empty()));
        lines.add(new TooltipLine.Fixed(buildChargesLine(item)));

        if (item.getDuration() > 0) {
            lines.add(new TooltipLine.Fixed(withWhiteShadow(Component.literal("\uE00C")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)))
                    .append(Component.literal(" " + StringUtils.formatDuration(item.getDuration()) + " ")
                            .withStyle(CommonStyles.LANGUAGE))
                    .append(Component.literal("Duration")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)))));
        }

        item.getNamedEffects().stream()
                .filter(effect -> effect.type() != com.wynntils.models.wynnitem.type.ConsumableEffect.DURATION)
                .map(effect -> Component.literal(
                                StringUtils.capitalizeFirst(effect.type().name().toLowerCase(Locale.ROOT))
                                        + " "
                                        + effect.value())
                        .withStyle(CommonStyles.LANGUAGE))
                .map(TooltipLine.Fixed::new)
                .forEach(lines::add);

        lines.add(new TooltipLine.Centered(divider()));
        if (item.getLevel() > 0) {
            lines.add(requirementLine(
                    " Combat Level",
                    String.valueOf(item.getLevel()),
                    Models.CharacterStats.getLevel() >= item.getLevel()));
        }
        lines.add(new TooltipLine.Centered(divider()));
        lines.addAll(CraftedTooltipIdentifications.buildLines(item, currentClass, decorator, style));
        return List.copyOf(lines);
    }

    private Component buildSyntheticTitle(CraftedItemProperty item, CraftedTooltipOptionDecorator decorator) {
        Component title = Component.literal(item.getName())
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.DARK_AQUA));
        return decorator == null ? title : decorator.getTitle(title);
    }

    private Component buildNameLine(String emblemFrame, String emblemSprite, Component title) {
        MutableComponent line = Component.literal("\uDAFF\uDFF0")
                .withStyle(style ->
                        style.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT).withShadowColor(0xffffff));
        line.append(Component.literal(emblemFrame).withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_FRAME_FONT)));
        line.append("\uDAFF\uDFCF");
        if (emblemSprite != null && !emblemSprite.isEmpty()) {
            line.append(Component.literal(emblemSprite)
                    .withStyle(
                            Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT).withColor(0x00eb1c)));
        }
        line.append(Component.literal("\uDB00\uDC05").withStyle(CommonStyles.SPACE));
        line.append(title);
        return line;
    }

    private Component buildTypeLine(String typeName) {
        MutableComponent line = Component.literal("\uDB00\uDC26").withStyle(CommonStyles.SPACE);
        line.append(BannerBoxFont.buildMessage(
                "Crafted",
                CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA),
                CommonColors.BLACK,
                "\uDB00\uDC02"));
        line.append(Component.literal("\uDB00\uDC01").withStyle(CommonStyles.SPACE));
        line.append(BannerBoxFont.buildMessage(
                StringUtils.capitalizeFirst(typeName.toLowerCase(Locale.ROOT)),
                CustomColor.fromInt(CRAFTED_ACCENT_COLOR),
                CommonColors.BLACK,
                ""));
        return line;
    }

    private Component buildDamageLine(List<Pair<DamageType, RangedValue>> damages) {
        MutableComponent line = Component.empty();
        for (Pair<DamageType, RangedValue> damage : damages) {
            line.append(withWhiteShadow(Component.literal(damage.a().getTooltipSprite())
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))));
            line.append(
                    Component.literal(" " + damage.b().low() + "-" + damage.b().high() + " ")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)));
        }
        return line;
    }

    private Component buildDefenceLine(List<Pair<Element, Integer>> defences) {
        MutableComponent line = Component.empty();
        for (Pair<Element, Integer> defence : defences) {
            line.append(withWhiteShadow(Component.literal(defence.a().getTooltipSprite())
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))));
            line.append(Component.literal(" " + StringUtils.toSignedCommaString(defence.b()) + " ")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.GRAY)));
        }
        return line;
    }

    private Component buildDurabilityLine(CraftedGearItem item) {
        MutableComponent line =
                buildMeter(item.getDurability().current(), item.getDurability().max());
        line.append(Component.literal(" Durability " + item.getDurability().current() + "/"
                        + item.getDurability().max())
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY)));
        return line;
    }

    private Component buildChargesLine(CraftedConsumableItem item) {
        MutableComponent line =
                buildMeter(item.getUses().current(), item.getUses().max());
        line.append(Component.literal(
                        " " + item.getUses().current() + "/" + item.getUses().max() + " ")
                .withStyle(CommonStyles.LANGUAGE));
        line.append(Component.literal("Charges")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY)));
        return line;
    }

    private MutableComponent buildMeter(int current, int maximum) {
        int fill = maximum <= 0
                ? 0
                : current >= maximum ? 35 : Math.clamp((int) Math.floor(current * 35d / maximum), 0, 35);
        return Component.literal("\uE023\uDAFF\uDFF7")
                .withStyle(Style.EMPTY
                        .withFont(IDENTIFICATION_METER_FONT)
                        .withColor(ChatFormatting.DARK_GRAY)
                        .withShadowColor(0xffffff))
                .append(Component.literal(String.valueOf((char) ('\uE000' + fill)))
                        .withStyle(Style.EMPTY
                                .withFont(IDENTIFICATION_METER_FONT)
                                .withColor(CRAFTED_ACCENT_COLOR)
                                .withShadowColor(0xffffff)));
    }

    private List<TooltipLine> buildGearRequirements(CraftedGearItem item) {
        GearRequirements requirements = item.getRequirements();
        List<TooltipLine> lines = new ArrayList<>();
        if (!requirements.skills().isEmpty()) {
            lines.add(new TooltipLine.Fixed(Component.empty()));
            lines.add(new TooltipLine.Centered(buildSkillIcons(requirements)));
            lines.add(new TooltipLine.Fixed(Component.empty()));
            lines.add(new TooltipLine.Centered(buildSkillValues(requirements)));
            lines.add(new TooltipLine.Fixed(Component.empty()));
        }

        requirements
                .quest()
                .ifPresent(quest -> lines.add(requirementLine(
                        " Quest",
                        StringUtils.shorten(quest, 10),
                        Models.Quest.getQuestFromName(quest)
                                .map(info -> info.status() == ActivityStatus.COMPLETED)
                                .orElse(false))));
        requirements
                .classType()
                .ifPresent(classType -> lines.add(requirementLine(
                        " Class Type", classType.getFullName(), Models.Character.getClassType() == classType)));
        if (requirements.level() > 0) {
            lines.add(requirementLine(
                    " Combat Level",
                    String.valueOf(requirements.level()),
                    Models.CharacterStats.getLevel() >= requirements.level()));
        }
        return lines;
    }

    private TooltipLine requirementLine(String label, String value, boolean fulfilled) {
        MutableComponent left =
                requirementIcon(fulfilled).append(Component.literal(label).withStyle(CommonStyles.LANGUAGE));
        Component right = Component.literal(value)
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY));
        return new TooltipLine.Aligned(left, right);
    }

    private Component buildSkillIcons(GearRequirements requirements) {
        MutableComponent line = Component.empty();
        for (Skill skill : Skill.values()) {
            int count = skillRequirement(requirements, skill);
            String frame = count == 0 ? "\uE007" : "\uE006";
            String sprite = String.valueOf((char) ((count == 0 ? '\uE010' : '\uE000') + skill.ordinal()));
            line.append(withWhiteShadow(Component.literal(frame)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_FRAME_FONT))
                    .append(Component.literal("\uDAFF\uDFE7"))
                    .append(Component.literal(sprite)
                            .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)))));
            line.append(Component.literal("\uDB00\uDC02").withStyle(CommonStyles.SPACE));
        }
        return line;
    }

    private Component buildSkillValues(GearRequirements requirements) {
        MutableComponent line = Component.empty();
        for (Skill skill : Skill.values()) {
            int count = skillRequirement(requirements, skill);
            boolean fulfilled = count == 0 || Models.SkillPoint.getTotalSkillPoints(skill) >= count;
            String icon = count == 0 ? "\uE005" : fulfilled ? "\uE006" : "\uE007";
            line.append(withWhiteShadow(Component.literal(icon + "\uDAFF\uDFFF")
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT))));
            line.append(Component.literal("\uDB00\uDC03").withStyle(CommonStyles.SPACE));
            line.append(Component.literal(String.valueOf(count))
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(count == 0 ? 0x555555 : fulfilled ? 0xacfac6 : 0xfaacac)));
            line.append(Component.literal("\uDB00\uDC04").withStyle(CommonStyles.SPACE));
        }
        return line;
    }

    private int skillRequirement(GearRequirements requirements, Skill skill) {
        return requirements.skills().stream()
                .filter(requirement -> requirement.a() == skill)
                .map(Pair::b)
                .findFirst()
                .orElse(0);
    }

    private MutableComponent requirementIcon(boolean fulfilled) {
        return withWhiteShadow(Component.literal((fulfilled ? "\uE006" : "\uE007") + "\uDAFF\uDFFF")
                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)));
    }

    private List<TooltipLine> buildPaginator(CraftedGearItem item) {
        int currentPage = item.currentPage();
        MutableComponent keyPrompt = Component.literal("\uF002")
                .withStyle(Style.EMPTY.withFont(CommonFonts.CHAT_TILE_FONT))
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

    private static Component divider() {
        return withWhiteShadow(Component.literal("\uE000")
                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_DIVIDER_FONT).withColor(CRAFTED_ACCENT_COLOR)));
    }

    private static MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }

    private record TooltipKey(ClassType currentClass, TooltipOptions options, int currentPage) {}
}

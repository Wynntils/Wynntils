/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.TooltipLayout;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipOptions.WeightDisplay;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.RerollableItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> the type of the item info
 * @param <U> the type of the item instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private static final int TOOLTIP_MIN_WIDTH = 140;
    private final IdentifiableItemProperty<T, U> itemInfo;
    private final boolean synthetic;
    private final List<Component> layoutSourceLines;
    private final Map<TooltipKey, List<Component>> cache = new HashMap<>();

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo,
            List<Component> header,
            List<Component> footer,
            String source,
            boolean synthetic) {
        super(header, footer, source);
        this.itemInfo = itemInfo;
        this.synthetic = synthetic;
        List<Component> sourceLines = new ArrayList<>(header.size() + footer.size());
        sourceLines.addAll(header);
        sourceLines.addAll(footer);
        this.layoutSourceLines = List.copyOf(sourceLines);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem, String source) {
        return new IdentifiableTooltipBuilder<>(identifiableItem, List.of(), List.of(), source, true);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildFromItemStack(
            ItemStack itemStack, IdentifiableItemProperty<T, U> identifiableItem, String source) {
        return fromTooltipLines(LoreUtils.getTooltipLines(itemStack), identifiableItem, source);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> fromTooltipLines(
            List<Component> tooltipLines, IdentifiableItemProperty<T, U> identifiableItem) {
        return fromTooltipLines(tooltipLines, identifiableItem, "");
    }

    private static <T, U> IdentifiableTooltipBuilder<T, U> fromTooltipLines(
            List<Component> tooltipLines, IdentifiableItemProperty<T, U> identifiableItem, String source) {
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltipLines);
        return new IdentifiableTooltipBuilder<>(identifiableItem, splitLore.a(), splitLore.b(), source, false);
    }

    public static IdentifiableTooltipBuilder fromParsedItemStack(
            ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return fromTooltipLines(LoreUtils.getTooltipLines(itemStack), itemInfo);
    }

    @Override
    public List<Component> getTooltipLines(ClassType currentClass, TooltipOptions options) {
        return getTooltipLines(currentClass, options, TOOLTIP_MIN_WIDTH);
    }

    public List<Component> getTooltipLines(ClassType currentClass, TooltipOptions options, int minimumWidth) {
        if (!synthetic && itemInfo instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            return super.getTooltipLines(
                    currentClass,
                    options.style(),
                    new TooltipOptionDecorator(itemInfo, options),
                    ItemWeightSource.NONE,
                    null);
        }

        TooltipKey key = new TooltipKey(currentClass, options, Math.max(TOOLTIP_MIN_WIDTH, minimumWidth));
        return cache.computeIfAbsent(
                key,
                ignored -> synthetic
                        ? buildSyntheticTooltip(currentClass, options, key.minimumWidth())
                        : buildUpdatedTooltip(currentClass, options, key.minimumWidth()));
    }

    private List<Component> buildSyntheticTooltip(ClassType currentClass, TooltipOptions options, int minimumWidth) {
        return prependSource(assemble(
                itemInfo, currentClass, options, buildHeader(itemInfo, options), buildMajorId(itemInfo), minimumWidth));
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return itemInfo instanceof GearTierItemProperty tierItem
                ? tierItem.getGearTier().getChatFormatting()
                : ChatFormatting.WHITE;
    }

    private List<Component> buildUpdatedTooltip(ClassType currentClass, TooltipOptions options, int minimumWidth) {
        List<TooltipLine> header = extractHeader(layoutSourceLines, itemInfo, options);
        List<TooltipLine> majorId = extractMajorId(layoutSourceLines);
        return prependSource(assemble(itemInfo, currentClass, options, header, majorId, minimumWidth));
    }

    @Override
    protected List<Component> decorateHeader(
            List<Component> header, TooltipIdentificationDecorator identificationDecorator) {
        if (identificationDecorator == null) return header;

        List<Component> decoratedHeader = new ArrayList<>(header);

        for (int i = 0; i < decoratedHeader.size(); i++) {
            MutableComponent line = decoratedHeader.get(i).copy();
            List<Component> siblings = line.getSiblings();
            for (int j = siblings.size() - 1; j >= 0; j--) {
                Component sibling = siblings.get(j);
                String text = sibling.getString().trim();
                if (!text.equals(itemInfo.getName()) && !text.endsWith(itemInfo.getName())) continue;

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
        if (itemInfo instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            return List.of();
        }

        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style, targetWidth);
    }

    private List<Component> assemble(
            IdentifiableItemProperty<?, ?> item,
            ClassType currentClass,
            TooltipOptions options,
            List<TooltipLine> header,
            List<TooltipLine> majorId,
            int minimumWidth) {
        GearTier tier = getGearTier(item);
        Sections sections = new Sections(
                header,
                buildWeights(item, options),
                buildRequirements(item),
                buildShiny(item, tier),
                buildReroll(item, tier),
                TooltipIdentifications.buildLines(
                        item, currentClass, new TooltipOptionDecorator(item, options), options.style()),
                majorId,
                buildPaginator(item));

        return TooltipLayout.align(sections.lines(tier, options.identificationDisplay()), minimumWidth);
    }

    private List<TooltipLine> buildHeader(IdentifiableItemProperty<?, ?> item, TooltipOptions options) {
        List<TooltipLine> header = new ArrayList<>();
        header.add(new TooltipLine.Fixed(Component.empty()));
        header.add(new TooltipLine.Fixed(buildNameLine(item, options)));
        header.add(new TooltipLine.Fixed(buildTypeLine(item)));

        if (item instanceof GearItem gearItem) {
            GearInfo info = gearItem.getItemInfo();
            Component tags = buildTagsLine(info);
            if (!tags.getString().isBlank()) header.add(new TooltipLine.Fixed(tags));

            header.add(new TooltipLine.Fixed(Component.empty()));
            buildOverview(info).stream().map(TooltipLine.Fixed::new).forEach(header::add);
        }
        return header;
    }

    private List<TooltipLine> extractHeader(
            List<Component> original, IdentifiableItemProperty<?, ?> item, TooltipOptions options) {
        int firstDivider = findFirstLineWithFont(original, CommonFonts.DIVIDER_FONT);
        int end = firstDivider < 0 ? original.size() : firstDivider;
        List<TooltipLine> header = new ArrayList<>(end);
        for (int i = 0; i < end; i++) {
            Component line = original.get(i);
            if (i == 1) {
                Component updatedTitle = line.copy();
                if (!TooltipUtils.replaceTrailingTitleComponent(
                        (MutableComponent) updatedTitle,
                        item.getName(),
                        buildDecoratedName(item, getGearTier(item), options))) {
                    updatedTitle = buildNameLine(item, options);
                }
                line = updatedTitle;
            }
            header.add(new TooltipLine.Fixed(line));
        }
        return header;
    }

    private List<TooltipLine> extractMajorId(List<Component> original) {
        int majorId = findFirstLineWithFont(original, CommonFonts.MAJOR_ID_FONT);
        int paginator = findFirstLineWithFont(original, CommonFonts.PAGE_FONT);
        if (majorId < 0) return List.of();

        int start = majorId;
        while (start > 0 && original.get(start - 1).getString().isBlank()) start--;
        List<TooltipLine> tail = new ArrayList<>();
        int end = paginator >= 0 ? paginator : original.size();
        for (int i = start; i < end; i++) {
            tail.add(new TooltipLine.Fixed(original.get(i)));
        }
        return tail;
    }

    private List<TooltipLine> buildWeights(IdentifiableItemProperty<?, ?> identifiableItem, TooltipOptions options) {
        if (!(identifiableItem instanceof GearItem item)) return List.of();
        if (item.getItemInstance().isEmpty() || options.itemWeightSource() == ItemWeightSource.NONE) {
            return List.of();
        }

        List<TooltipLine> lines = new ArrayList<>();
        appendWeightSource(lines, item, options, ItemWeightSource.NORI);
        appendWeightSource(lines, item, options, ItemWeightSource.WYNNPOOL);
        return lines;
    }

    private void appendWeightSource(
            List<TooltipLine> lines, GearItem item, TooltipOptions options, ItemWeightSource source) {
        if (options.itemWeightSource() != ItemWeightSource.ALL && options.itemWeightSource() != source) return;

        List<ItemWeighting> weightings = Services.ItemWeight.getItemWeighting(item.getName(), source);
        if (weightings.isEmpty()) return;

        if (!lines.isEmpty()) {
            lines.add(new TooltipLine.Fixed(Component.empty()));
        }

        Component sourceName = Component.literal("\u2696")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.DEFAULT_FONT)
                        .withColor(source.getColor().asInt()))
                .append(Component.literal(" " + StringUtils.capitalized(source.name()))
                        .withStyle(CommonStyles.LANGUAGE));
        lines.add(new TooltipLine.Fixed(sourceName));
        for (ItemWeighting weighting : weightings) {
            float percentage = Services.ItemWeight.calculateWeighting(weighting, item);
            Component percentageComponent = ColorScaleUtils.getPercentageTextComponent(
                            options.colorMap(), percentage, options.colorLerp(), options.decimalPlaces())
                    .withStyle(style -> style.withFont(CommonFonts.LANGUAGE_FONT));

            lines.add(new TooltipLine.Aligned(
                    Component.literal(weighting.weightName() + " Scale").withStyle(CommonStyles.LANGUAGE),
                    percentageComponent));

            if (options.weightDisplay() != WeightDisplay.OVERALL) {
                appendDetailedWeightLines(lines, weighting, item, options);
            }
        }
    }

    private void appendDetailedWeightLines(
            List<TooltipLine> lines, ItemWeighting weighting, GearItem item, TooltipOptions options) {
        Map<StatType, Pair<Float, Float>> statWeights = Services.ItemWeight.getStatWeights(weighting, item);
        statWeights.forEach((statType, weight) -> {
            String displayName = statType.getDisplayName() + " ";
            if (statType.getUnit() == StatUnit.RAW) displayName += "Raw ";

            float percentage = options.weightDisplay() == WeightDisplay.DISTRIBUTION
                    ? weight.b()
                    : (weight.a() / 100f) * weight.b();
            Component left = Component.literal(new DecimalFormat("#.#").format(weight.a()) + "%")
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(" " + displayName)
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_FONT)
                                    .withColor(ChatFormatting.GRAY)));
            Component right = ColorScaleUtils.getPercentageTextComponent(
                            options.colorMap(), percentage, options.colorLerp(), options.decimalPlaces())
                    .withStyle(style -> style.withFont(CommonFonts.LANGUAGE_FONT));
            lines.add(new TooltipLine.Aligned(left, right));
        });
    }

    private List<TooltipLine> buildRequirements(IdentifiableItemProperty<?, ?> item) {
        if (item instanceof GearItem gearItem) return buildGearRequirements(gearItem);
        if (!(item instanceof LeveledItemProperty leveledItem) || leveledItem.getLevel() <= 0) return List.of();

        return List.of(requirementLine(
                " Combat Level",
                String.valueOf(leveledItem.getLevel()),
                Models.CharacterStats.getLevel() >= leveledItem.getLevel()));
    }

    private List<TooltipLine> buildGearRequirements(GearItem item) {
        GearInfo info = item.getItemInfo();
        List<TooltipLine> requirements = new ArrayList<>();

        if (!info.requirements().skills().isEmpty()) {
            requirements.add(new TooltipLine.Fixed(Component.empty()));
            requirements.add(new TooltipLine.Centered(buildSkillIcons(info)));
            requirements.add(new TooltipLine.Fixed(Component.empty()));
            requirements.add(new TooltipLine.Centered(
                    buildSkillValues(info, item.getItemInstance().orElse(null))));
            requirements.add(new TooltipLine.Fixed(Component.empty()));
        }

        info.requirements()
                .quest()
                .ifPresent(quest -> requirements.add(
                        requirementLine(" Quest", StringUtils.shorten(quest, 10), item.meetsActualRequirements())));
        info.requirements()
                .classType()
                .ifPresent(classType -> requirements.add(requirementLine(
                        " Class Type", classType.getFullName(), Models.Character.getClassType() == classType)));
        if (info.requirements().level() > 0) {
            requirements.add(requirementLine(
                    " Combat Level",
                    String.valueOf(info.requirements().level()),
                    Models.CharacterStats.getLevel() >= info.requirements().level()));
        }
        return requirements;
    }

    private TooltipLine requirementLine(String label, String value, boolean fulfilled) {
        MutableComponent left =
                requirementIcon(fulfilled).append(Component.literal(label).withStyle(CommonStyles.LANGUAGE));
        Component right = Component.literal(value)
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.GRAY));
        return new TooltipLine.Aligned(left, right);
    }

    private Component buildSkillIcons(GearInfo info) {
        MutableComponent line = Component.empty();
        for (Skill skill : Skill.values()) {
            int count = skillRequirement(info, skill);
            String frame = count == 0 ? "\uE007" : skillFrame(info.tier());
            String sprite = String.valueOf((char) ((count == 0 ? '\uE010' : '\uE000') + skill.ordinal()));
            line.append(withWhiteShadow(Component.literal(frame)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.REQUIREMENT_FRAME_FONT))
                    .append(Component.literal("\uDAFF\uDFE7"))
                    .append(Component.literal(sprite)
                            .withStyle(Style.EMPTY.withFont(CommonFonts.REQUIREMENT_SPRITE_FONT)))));
            line.append(Component.literal("\uDB00\uDC02").withStyle(CommonStyles.SPACE));
        }
        return line;
    }

    private Component buildSkillValues(GearInfo info, GearInstance instance) {
        MutableComponent line = Component.empty();
        for (Skill skill : Skill.values()) {
            int count = skillRequirement(info, skill);
            boolean fulfilled = count == 0
                    || instance != null && instance.meetsRequirements()
                    || Models.SkillPoint.getTotalSkillPoints(skill) >= count;
            String icon = count == 0 ? "\uE005" : fulfilled ? "\uE006" : "\uE007";
            line.append(withWhiteShadow(Component.literal(icon + "\uDAFF\uDFFF")
                    .withStyle(Style.EMPTY.withFont(CommonFonts.REQUIREMENT_SPRITE_FONT))));
            line.append(Component.literal("\uDB00\uDC03").withStyle(CommonStyles.SPACE));
            line.append(Component.literal(String.valueOf(count))
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_FONT)
                            .withColor(count == 0 ? 0x555555 : fulfilled ? 0xacfac6 : 0xfaacac)));
            line.append(Component.literal("\uDB00\uDC04").withStyle(CommonStyles.SPACE));
        }
        return line;
    }

    private List<TooltipLine> buildShiny(IdentifiableItemProperty<?, ?> item, GearTier tier) {
        if (!(item instanceof ShinyItemProperty shinyItem)) return List.of();

        return shinyItem
                .getShinyStat()
                .<List<TooltipLine>>map(shiny -> List.of(new TooltipLine.Aligned(
                        Component.empty()
                                .append(withWhiteShadow(Component.literal("\uE04F")
                                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT))))
                                .append(Component.literal(" " + shiny.statType().displayName())
                                        .withStyle(Style.EMPTY
                                                .withFont(CommonFonts.LANGUAGE_FONT)
                                                .withColor(dividerColor(tier)))),
                        buildShinyValue(shiny, tier))))
                .orElseGet(List::of);
    }

    private List<TooltipLine> buildReroll(IdentifiableItemProperty<?, ?> item, GearTier tier) {
        if (!(item instanceof RerollableItemProperty rerollableItem)) return List.of();

        int rerolls = rerollableItem.getRerollCount();
        if (rerolls <= 0) return List.of();

        int color = dividerColor(tier);
        MutableComponent line = Component.empty()
                .append(BannerBoxFont.buildMessage(
                        String.valueOf(rerolls), CustomColor.fromInt(color), CommonColors.BLACK, "\uDB00\uDC02"))
                .append(Component.literal("\uDAFF\uDFFF").withStyle(CommonStyles.SPACE))
                .append(Component.literal("\uE005")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_BANNER_FONT)
                                .withColor(color)
                                .withShadowColor(0xffffff)))
                .append(withWhiteShadow(Component.literal("\uDAFF\uDFF6\uF005")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_BANNER_FONT))));
        return List.of(new TooltipLine.Aligned(Component.empty(), line));
    }

    private Component buildShinyValue(ShinyStat shiny, GearTier tier) {
        MutableComponent value =
                Component.literal(String.valueOf(shiny.value())).withStyle(CommonStyles.LANGUAGE);
        if (shiny.shinyRerolls() > 0) {
            value.append(" ")
                    .append(BannerBoxFont.buildMessage(
                            String.valueOf(shiny.shinyRerolls()),
                            CustomColor.fromInt(dividerColor(tier)),
                            CommonColors.BLACK,
                            "\uDB00\uDC02"));
        }
        return value;
    }

    private List<TooltipLine> buildMajorId(IdentifiableItemProperty<?, ?> identifiableItem) {
        if (!(identifiableItem instanceof GearItem item)) return List.of();

        return item.getItemInfo()
                .fixedStats()
                .majorIds()
                .<List<TooltipLine>>map(major -> {
                    List<TooltipLine> lines = new ArrayList<>();
                    lines.add(new TooltipLine.Fixed(Component.empty()));

                    MutableComponent text = Component.empty()
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_FONT)
                                    .withColor(dividerColor(item.getGearTier())))
                            .append(Component.literal("\uE000")
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.MAJOR_ID_FONT)))
                            .append(Component.literal("\uDB00\uDC02"))
                            .append(Component.literal(major.name() + ": "))
                            .append(major.lore().getComponent());
                    ComponentUtils.splitComponent(text, TOOLTIP_MIN_WIDTH).stream()
                            .map(TooltipLine.Fixed::new)
                            .forEach(lines::add);
                    return lines;
                })
                .orElseGet(List::of);
    }

    private List<TooltipLine> buildPaginator(IdentifiableItemProperty<?, ?> item) {
        if (!(item instanceof PagedItemProperty pagedItem)) return List.of();

        int currentPage = pagedItem.currentPage();
        MutableComponent keyPrompt = Component.literal("\uF002")
                .withStyle(Style.EMPTY.withFont(CommonFonts.CHAT_TILE_FONT))
                .append(Component.literal("\uDAFF\uDF98\uDB00\uDC3F").withStyle(CommonStyles.LANGUAGE));
        int keyPromptAdvance = McUtils.mc().font.width(keyPrompt);
        MutableComponent paginator = Component.empty().append(keyPrompt);
        for (int page = 0; page < 3; page++) {
            paginator.append(Component.literal("\uE000")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.PAGE_FONT)
                            .withColor(page == currentPage ? 0xffea80 : 0x455449)
                            .withShadowColor(0xffffff)));
            if (page < 2) paginator.append(Component.literal("\uDB00\uDC04").withStyle(CommonStyles.LANGUAGE));
        }
        paginator.append(Component.literal(Managers.Font.calculateOffset(0, keyPromptAdvance))
                .withStyle(CommonStyles.SPACE));
        return List.of(new TooltipLine.Centered(paginator), new TooltipLine.Fixed(Component.empty()));
    }

    private Component buildNameLine(IdentifiableItemProperty<?, ?> item, TooltipOptions options) {
        GearType type = getGearType(item);
        String emblemFrame =
                item instanceof GearItem gearItem ? gearItem.getItemInfo().getEmblemFrameCode() : type.getFrameCode();
        String emblemSprite = item instanceof GearItem gearItem
                ? gearItem.getItemInfo().getEmblemSpriteCode()
                : type.getFrameSpriteCode();
        return buildNameLine(emblemFrame, emblemSprite, buildDecoratedName(item, getGearTier(item), options));
    }

    private Component buildNameLine(String emblemFrame, String emblemSprite, Component title) {
        MutableComponent line =
                Component.literal("\uDAFF\uDFF0").withStyle(style -> style.withFont(CommonFonts.LANGUAGE_FONT));
        line.append(Component.literal(emblemFrame).withStyle(Style.EMPTY.withFont(CommonFonts.EMBLEM_FRAME_FONT)));
        line.append("\uDAFF\uDFCF");
        line.append(Component.literal(emblemSprite)
                .withStyle(Style.EMPTY.withFont(CommonFonts.EMBLEM_SPRITE_FONT).withColor(0x00eb1c)));
        line.append(Component.literal("\uDB00\uDC05").withStyle(CommonStyles.SPACE));
        line.append(title);
        return line;
    }

    private MutableComponent buildDecoratedName(
            IdentifiableItemProperty<?, ?> item, GearTier tier, TooltipOptions options) {
        boolean shiny = item instanceof ShinyItemProperty shinyItem
                && shinyItem.getShinyStat().isPresent();
        String name = shiny ? "Shiny " + item.getName() : item.getName();
        Component title = Component.literal(name)
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(tier.getChatFormatting()));
        return new TooltipOptionDecorator(item, options).getTitle(title);
    }

    private Component buildTypeLine(IdentifiableItemProperty<?, ?> item) {
        GearType type = getGearType(item);
        String typeName = type.isReward() ? StringUtils.capitalizeFirst(type.getModelKey()) : type.name();
        return buildTypeLine(getGearTier(item), typeName, getRestrictions(item));
    }

    private Component buildTypeLine(GearTier tier, String typeName, GearRestrictions restrictions) {
        Pair<String, String> restrictionIcon =
                switch (restrictions) {
                    case UNTRADABLE -> Pair.of("\uE002", "\uF002");
                    case QUEST_ITEM -> Pair.of("\uE003", "\uF003");
                    default -> null;
                };
        MutableComponent line = Component.literal("\uDB00\uDC26").withStyle(CommonStyles.SPACE);
        line.append(BannerBoxFont.buildMessage(
                tier.getName(),
                CustomColor.fromChatFormatting(tier.getChatFormatting()),
                CommonColors.BLACK,
                "\uDB00\uDC02"));
        line.append(Component.literal("\uDB00\uDC01").withStyle(CommonStyles.SPACE));
        line.append(BannerBoxFont.buildMessage(
                typeName,
                CustomColor.fromInt(dividerColor(tier)),
                CommonColors.BLACK,
                restrictionIcon != null ? "\uDB00\uDC02" : ""));
        if (restrictionIcon != null) {
            line.append(Component.literal(restrictionIcon.a())
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.TOOLTIP_BANNER_FONT)
                            .withColor(0xff4242)
                            .withShadowColor(0xffffff)));
            line.append(withWhiteShadow(Component.literal("\uDAFF\uDFF6" + restrictionIcon.b())
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_BANNER_FONT))));
        }
        return line;
    }

    private GearRestrictions getRestrictions(IdentifiableItemProperty<?, ?> item) {
        return switch (item.getItemInfo()) {
            case GearInfo info -> info.metaInfo().restrictions();
            case CharmInfo info -> info.metaInfo().restrictions();
            case TomeInfo info -> info.metaInfo().restrictions();
            default -> GearRestrictions.NONE;
        };
    }

    private GearTier getGearTier(IdentifiableItemProperty<?, ?> item) {
        if (item instanceof GearTierItemProperty tierItem) return tierItem.getGearTier();

        throw new IllegalArgumentException(
                "Identifiable item has no gear tier: " + item.getClass().getName());
    }

    private GearType getGearType(IdentifiableItemProperty<?, ?> item) {
        if (item instanceof GearTypeItemProperty typeItem) return typeItem.getGearType();

        throw new IllegalArgumentException(
                "Identifiable item has no gear type: " + item.getClass().getName());
    }

    private Component buildTagsLine(GearInfo info) {
        Set<Element> elements = new LinkedHashSet<>();
        info.fixedStats().damages().forEach(damage -> damage.a().getElement().ifPresent(elements::add));
        info.fixedStats().defences().forEach(defence -> elements.add(defence.a()));
        List<Element> sortedElements = elements.stream()
                .sorted((left, right) -> Integer.compare(left.getEncodingId(), right.getEncodingId()))
                .toList();
        CustomColor tierColor = CustomColor.fromInt(dividerColor(info.tier()));
        MutableComponent line = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_FONT)
                        .withColor(tierColor.asInt())
                        .withShadowColor(0xffffff));
        boolean hasSet = info.setInfo().isPresent();

        info.setInfo()
                .ifPresent(set -> line.append(Component.literal("\uDB00\uDC26"))
                        .append(BannerBoxFont.buildMessage(set.name() + " set", tierColor, CommonColors.BLACK, "")));
        if (sortedElements.isEmpty()) return line;

        if (hasSet) {
            line.append(" ");
        } else {
            line.append("\uDB00\uDC26");
        }
        appendElementStrip(line, sortedElements);

        if (!hasSet && sortedElements.size() == 1) {
            line.append("\uDAFF\uDFFF");
            line.append(BannerBoxFont.buildMessage(
                    sortedElements.getFirst().getDisplayName(), tierColor, CommonColors.BLACK, ""));
        }
        return line;
    }

    private void appendElementStrip(MutableComponent line, List<Element> elements) {
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            line.append(Component.literal(elementBannerGlyph(element))
                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_BANNER_FONT)));
            line.append(Component.literal("\uDAFF\uDFF6" + elementBannerOverlayGlyph(element))
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.TOOLTIP_BANNER_FONT)
                            .withColor(ChatFormatting.WHITE)));
            if (i < elements.size() - 1) line.append("\uDAFF\uDFFF");
        }
    }

    private String elementBannerGlyph(Element element) {
        return switch (element) {
            case EARTH -> "\uE006";
            case THUNDER -> "\uE007";
            case WATER -> "\uE008";
            case FIRE -> "\uE009";
            case AIR -> "\uE00A";
        };
    }

    private String elementBannerOverlayGlyph(Element element) {
        return switch (element) {
            case EARTH -> "\uF006";
            case THUNDER -> "\uF007";
            case WATER -> "\uF008";
            case FIRE -> "\uF009";
            case AIR -> "\uF00A";
        };
    }

    private List<Component> buildOverview(GearInfo info) {
        List<Component> overview = new ArrayList<>();
        if (info.type().isWeapon()) {
            overview.add(
                    Component.literal(String.format("%,d", info.fixedStats().averageDps()))
                            .withStyle(Style.EMPTY.withFont(CommonFonts.QUAD_12).withColor(dividerColor(info.tier())))
                            .append(Component.literal(" DPS").withStyle(CommonStyles.LANGUAGE)));
        } else if (info.type().isArmor() || info.type().isAccessory()) {
            overview.add(Component.literal(
                            StringUtils.toSignedCommaString(info.fixedStats().healthBuff()))
                    .withStyle(Style.EMPTY.withFont(CommonFonts.QUAD_12).withColor(dividerColor(info.tier())))
                    .append(Component.literal(" Health").withStyle(CommonStyles.LANGUAGE)));
        }

        info.fixedStats()
                .attackSpeed()
                .ifPresent(speed -> overview.add(Component.empty()
                        .append(Component.literal("\uE007")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.ATTRIBUTE_SPRITE_FONT)))
                        .append(Component.literal(" " + speed.getName() + " ")
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_FONT)
                                        .withColor(ChatFormatting.GRAY)))
                        .append(Component.literal("(" + speed.getHitsPerSecond() + " hits/s)")
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_FONT)
                                        .withColor(ChatFormatting.DARK_GRAY)))));

        if (!info.fixedStats().damages().isEmpty())
            overview.add(buildDamageLine(info.fixedStats().damages()));
        if (!info.fixedStats().defences().isEmpty())
            overview.add(buildDefenceLine(info.fixedStats().defences()));
        return overview;
    }

    private Component buildDamageLine(List<Pair<DamageType, RangedValue>> damages) {
        MutableComponent line = Component.empty();
        for (Pair<DamageType, RangedValue> damage : damages) {
            line.append(withWhiteShadow(Component.literal(damage.a().getTooltipSprite())
                    .withStyle(Style.EMPTY.withFont(CommonFonts.ATTRIBUTE_SPRITE_FONT))));
            line.append(Component.literal(
                            " " + damage.b().low() + "-" + damage.b().high() + " ")
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));
        }
        return line;
    }

    private Component buildDefenceLine(List<Pair<Element, Integer>> defences) {
        MutableComponent line = Component.empty();
        for (Pair<Element, Integer> defence : defences) {
            line.append(withWhiteShadow(Component.literal(defence.a().getTooltipSprite())
                    .withStyle(Style.EMPTY.withFont(CommonFonts.ATTRIBUTE_SPRITE_FONT))));
            line.append(Component.literal(" " + StringUtils.toSignedCommaString(defence.b()) + " ")
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));
        }
        return line;
    }

    private MutableComponent requirementIcon(boolean fulfilled) {
        return withWhiteShadow(Component.literal((fulfilled ? "\uE006" : "\uE007") + "\uDAFF\uDFFF")
                .withStyle(Style.EMPTY.withFont(CommonFonts.REQUIREMENT_SPRITE_FONT)));
    }

    private static MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xffffff))
                .append(component.copy());
    }

    private int skillRequirement(GearInfo info, Skill skill) {
        return info.requirements().skills().stream()
                .filter(requirement -> requirement.a() == skill)
                .map(Pair::b)
                .findFirst()
                .orElse(0);
    }

    private String skillFrame(GearTier tier) {
        GearTier[] tiers = GearTier.validValues();
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i] == tier) return String.valueOf((char) ('\uE000' + i));
        }
        return "\uE007";
    }

    private static Component divider(GearTier tier) {
        return withWhiteShadow(Component.literal("\uE000")
                .withStyle(Style.EMPTY.withFont(CommonFonts.DIVIDER_FONT).withColor(dividerColor(tier))));
    }

    private static Component identificationDivider(GearTier tier, IdentificationDisplay display) {
        if (display == IdentificationDisplay.PERCENTAGE) return divider(tier);

        String label =
                switch (display) {
                    case INTERNAL_ROLL -> "Internal Roll";
                    case RANGE -> "Stat ranges";
                    case REROLL -> "Reroll chance";
                    case PERCENTAGE -> throw new IllegalStateException();
                };
        int color = dividerColor(tier);
        return withWhiteShadow(Component.literal("\uE000")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.IDENTIFICATION_DIVIDER_FONT)
                        .withColor(color))
                .append(Component.literal(" " + label + " ")
                        .withStyle(
                                Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(color)))
                .append(Component.literal("\uE001")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.IDENTIFICATION_DIVIDER_FONT)
                                .withColor(color))));
    }

    private static int dividerColor(GearTier tier) {
        return switch (tier) {
            case NORMAL -> 0xe0e0e0;
            case UNIQUE -> 0xfff2b3;
            case RARE -> 0xf2c2f2;
            case LEGENDARY -> 0xc2f2f2;
            case FABLED -> 0xf2c2c2;
            case MYTHIC -> 0xe0b3e6;
            default -> 0xffffff;
        };
    }

    private int findFirstLineWithFont(List<Component> lines, FontDescription font) {
        for (int i = 0; i < lines.size(); i++) {
            if (TooltipUtils.containsFont(lines.get(i), font)) return i;
        }
        return -1;
    }

    private record Sections(
            List<TooltipLine> header,
            List<TooltipLine> weights,
            List<TooltipLine> requirements,
            List<TooltipLine> shiny,
            List<TooltipLine> reroll,
            List<TooltipLine> stats,
            List<TooltipLine> majorId,
            List<TooltipLine> paginator) {
        private List<TooltipLine> lines(GearTier tier, IdentificationDisplay identificationDisplay) {
            List<TooltipLine> lines = new ArrayList<>();
            lines.addAll(header);
            if (!weights.isEmpty()) {
                lines.add(new TooltipLine.Centered(divider(tier)));
                lines.addAll(weights);
            }
            lines.add(new TooltipLine.Centered(divider(tier)));
            lines.addAll(requirements);
            if (!shiny.isEmpty()) {
                lines.add(new TooltipLine.Centered(divider(tier)));
                lines.addAll(shiny);
            }
            if (!reroll.isEmpty()) {
                lines.add(new TooltipLine.Centered(identificationDivider(tier, identificationDisplay)));
                lines.addAll(reroll);
            }
            if (reroll.isEmpty()) {
                lines.add(new TooltipLine.Centered(identificationDivider(tier, identificationDisplay)));
            }
            lines.addAll(stats);
            lines.addAll(majorId);
            lines.addAll(paginator);
            return lines;
        }
    }

    private record TooltipKey(ClassType currentClass, TooltipOptions options, int minimumWidth) {}
}

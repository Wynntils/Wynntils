/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.IdentificationLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class CraftedConsumableTooltipBuilder extends TooltipBuilder {
    private static final int LORE_MAX_WIDTH = 160;
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.HEXAGON, 4);
    private static final Pattern ITEM_NAME_PATTERN =
            Pattern.compile("§f\uDAFF\uDFF0.\uDAFF\uDFCF§#00eb1cff.§f\uDB00\uDC05§3(?<itemName>.+)");

    private final List<Component> parsedHeader;

    private final CraftedConsumableItem item;

    private CraftedConsumableTooltipBuilder(
            CraftedConsumableItem item, ItemSourceType sourceType, String source, List<Component> parsedHeader) {
        super(sourceType, source);

        this.item = item;
        this.parsedHeader = parsedHeader;
    }

    public static CraftedConsumableTooltipBuilder buildNewItem(
            CraftedConsumableItem craftedConsumableItem, ItemSourceType sourceType, String source) {
        return new CraftedConsumableTooltipBuilder(craftedConsumableItem, sourceType, source, null);
    }

    public static CraftedConsumableTooltipBuilder buildFromItemStack(
            ItemStack itemStack, CraftedConsumableItem craftedConsumableItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        return new CraftedConsumableTooltipBuilder(
                craftedConsumableItem, ItemSourceType.ITEM, "", extractHeader(tooltips));
    }

    private List<TooltipElement> buildNewHeader(String source) {
        List<TooltipElement> lines = new ArrayList<>();

        if (!source.isEmpty()) {
            lines.add(buildSourceLine());
        }

        lines.add(TooltipLine.empty());

        String itemName = item.getName();

        Component nameComponent = buildItemName(
                item,
                itemName,
                Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(GearTier.CRAFTED.getChatFormatting()));

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                getItemEmblemSprite(),
                nameComponent,
                BannerBoxFont.buildMessage(
                        GearTier.CRAFTED.getApiName(),
                        CustomColor.fromChatFormatting(GearTier.CRAFTED.getChatFormatting()),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage(
                        item.getConsumableType().name(),
                        GearTier.CRAFTED.getSecondaryColor(),
                        CommonColors.BLACK,
                        "")));

        lines.add(TooltipLine.empty());

        MutableComponent chargesLine = Component.empty()
                .append(TooltipIdentificationMeterFont.buildCounterSingleLayerMeter(
                        item.getUses(),
                        GearTier.CRAFTED.getSecondaryColor(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        ""))
                .append(Component.literal(" " + item.getUses()).withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" Charges").withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));

        lines.add(TooltipLine.left(chargesLine));

        MutableComponent durationLine = Component.empty()
                .append(Component.literal("\uE00C")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                                .withoutShadow()))
                .append(Component.literal(" " + StringUtils.formatDuration(item.getDuration(), false))
                        .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                .append(Component.literal(" Duration").withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));

        lines.add(TooltipLine.left(durationLine));

        if (item.getConsumableType() == ConsumableType.SCROLL) {
            MutableComponent areaOfEffectLine = Component.empty()
                    .append(Component.literal("\uDAFF\uDFFF\uE00D\uDAFF\uDFFF")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                                    .withoutShadow()))
                    .append(Component.literal(" 10 Blocks").withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT))
                    .append(Component.literal(" Area of Effect").withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));

            lines.add(TooltipLine.left(areaOfEffectLine));
        }

        return lines;
    }

    private List<TooltipElement> decorateHeader() {
        List<TooltipElement> header = new ArrayList<>();

        boolean updatedName = false;
        for (Component line : parsedHeader) {
            StyledText styledText = StyledText.fromComponent(line);

            StyledTextPart lastPart = styledText.getLastPart();
            if (lastPart != null) {
                Matcher matcher = styledText.getMatcher(ITEM_NAME_PATTERN);
                if (!updatedName && matcher.matches()) {
                    String itemName = item.getName();

                    Component nameComponent = buildItemName(
                            item, itemName, lastPart.getPartStyle().getStyle());

                    styledText = styledText
                            .replaceFirst(Pattern.quote(itemName), "")
                            .append(StyledText.fromComponent(nameComponent));

                    updatedName = true;

                    header.add(TooltipLine.left(styledText.getComponent()));
                    continue;
                }
            }

            header.add(TooltipLine.left(line));
        }

        return header;
    }

    private static Component buildItemName(CraftedConsumableItem item, String itemName, Style style) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        MutableComponent nameComponent = Component.literal(itemName).withStyle(style);

        if (options.craftedOverallRollStyle() == TooltipOptions.RollStyle.PERCENTAGE) {
            nameComponent.append(ColorScaleUtils.getPercentageTextComponent(
                    options.colorMap(),
                    item.getOverallPercentage(),
                    options.colorLerp(),
                    item.hasFullPerfectInternalRolls(),
                    options.decimalPlaces(),
                    true));
        } else if (options.craftedOverallRollStyle() == TooltipOptions.RollStyle.WHEEL) {
            nameComponent
                    .append(Component.literal(" "))
                    .append(ColorScaleUtils.getWheelTextComponent(
                            options.colorMap(),
                            item.getOverallPercentage(),
                            options.colorLerp(),
                            item.hasFullPerfectInternalRolls()));
        }

        return nameComponent;
    }

    private List<TooltipElement> buildRequirements() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(GearTier.CRAFTED));

        int levelRequirement = item.getLevel();
        if (levelRequirement > 0) {
            lines.add(RequirementsLineBuilder.buildLevelRequirementLine(levelRequirement));
        }

        return lines;
    }

    private List<TooltipElement> buildIdentificationLines() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(GearTier.CRAFTED));

        lines.addAll(IdentificationLineBuilder.buildIdentificationLines(
                item.getIdentifications(),
                null,
                true,
                Handlers.Tooltip.getTooltipOptions().craftedRollStyle()));

        return lines;
    }

    private List<TooltipElement> buildFooter() {
        List<TooltipElement> lines = new ArrayList<>();

        if (!item.getLore().isEmpty()) {
            lines.add(TooltipLine.empty());

            Component lore = Component.literal(item.getLore())
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.DARK_GRAY)
                            .withItalic(true));

            lines.add(TooltipWrappedText.center(lore, LORE_MAX_WIDTH));
        }

        if (!item.getCreator().isEmpty()) {
            Component creatorLine = Component.literal("Crafted by " + item.getCreator())
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(GearTier.CRAFTED.getSecondaryColor().asInt()));

            lines.add(TooltipLine.center(creatorLine));
        }

        if (sourceType == ItemSourceType.ITEM) {
            lines.add(TooltipLine.empty());

            Component consumeLine = Component.empty()
                    .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class)
                            .withoutShadow())
                    .append(Component.literal(" Right-Click to Consume")
                            .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
            lines.add(TooltipLine.center(consumeLine));
        }

        return lines;
    }

    private ItemEmblemSprite getItemEmblemSprite() {
        return switch (item.getConsumableType()) {
            case FOOD -> ItemEmblemSprite.FOOD;
            case SCROLL -> ItemEmblemSprite.SCROLL;
            default -> ItemEmblemSprite.POTION;
        };
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return GearTier.CRAFTED.getChatFormatting();
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();
        List<TooltipElement> requirements = buildRequirements();
        List<TooltipElement> identifications = buildIdentificationLines();
        List<TooltipElement> footer = buildFooter();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(requirements);
        lines.addAll(identifications);
        lines.addAll(footer);

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}

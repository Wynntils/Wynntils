/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.DurabilityLineBuilder;
import com.wynntils.handlers.tooltip.components.HeaderStatsLineBuilder;
import com.wynntils.handlers.tooltip.components.IdentificationLineBuilder;
import com.wynntils.handlers.tooltip.components.LoreLineBuilder;
import com.wynntils.handlers.tooltip.components.PaginatorLineBuilder;
import com.wynntils.handlers.tooltip.components.PowderLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.RollStyle;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.wynnitem.type.ItemEmblem;
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

public class CraftedGearTooltipBuilder extends TooltipBuilder {
    private static final Pattern ITEM_NAME_PATTERN =
            Pattern.compile("§f\uDAFF\uDFF0.\uDAFF\uDFCF§#00eb1cff.§f\uDB00\uDC05§3(?<itemName>.+)");

    private final List<Component> parsedHeader;
    private final int page;

    private final CraftedGearItem item;

    private CraftedGearTooltipBuilder(
            CraftedGearItem item, ItemSourceType sourceType, String source, List<Component> parsedHeader, int page) {
        super(sourceType, source);

        this.item = item;
        this.parsedHeader = parsedHeader;
        this.page = page;
    }

    public static CraftedGearTooltipBuilder buildNewItem(
            CraftedGearItem craftedGearItem, ItemSourceType sourceType, String source, int page) {
        return new CraftedGearTooltipBuilder(craftedGearItem, sourceType, source, null, page);
    }

    public static CraftedGearTooltipBuilder buildFromItemStack(
            ItemStack itemStack, CraftedGearItem craftedGearItem, int page) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        return new CraftedGearTooltipBuilder(craftedGearItem, ItemSourceType.ITEM, "", extractHeader(tooltips), page);
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
                ItemEmblem.fromCraftedItem(item.getGearType()),
                getItemEmblemSprite(),
                nameComponent,
                BannerBoxFont.buildMessage(
                        GearTier.CRAFTED.getApiName(),
                        CustomColor.fromChatFormatting(GearTier.CRAFTED.getChatFormatting()),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage(
                        item.getGearType().getModelKey(),
                        GearTier.CRAFTED.getSecondaryColor(),
                        CommonColors.BLACK,
                        "")));

        lines.add(TooltipLine.empty());

        if (item.getGearType().isWeapon()) {
            lines.add(HeaderStatsLineBuilder.buildDpsLine(item.getDps(), GearTier.CRAFTED));

            lines.add(DurabilityLineBuilder.buildDurabilityLine(GearTier.CRAFTED, item.getDurability()));

            if (item.getAttackSpeed().isPresent()) {
                GearAttackSpeed attackSpeed = item.getAttackSpeed().get();
                lines.add(HeaderStatsLineBuilder.buildAttackSpeedLine(attackSpeed));
            }

            if (!item.getDamages().isEmpty()) {
                lines.add(HeaderStatsLineBuilder.buildDamageGrid(item.getDamages()));
            }
        } else {
            lines.add(HeaderStatsLineBuilder.buildHealthLine(item.getHealth(), GearTier.CRAFTED));

            lines.add(DurabilityLineBuilder.buildDurabilityLine(GearTier.CRAFTED, item.getDurability()));

            lines.addAll(HeaderStatsLineBuilder.buildDefenceLines(item.getDefences()));
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

                    styledText = styledText.replaceFirst(itemName, "").append(StyledText.fromComponent(nameComponent));

                    updatedName = true;

                    header.add(TooltipLine.left(styledText.getComponent()));
                    continue;
                }
            }

            header.add(TooltipLine.left(line));
        }

        return header;
    }

    private static Component buildItemName(CraftedGearItem item, String itemName, Style style) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        MutableComponent nameComponent = Component.literal(itemName).withStyle(style);

        if (!item.hasParsedPage(0)) return nameComponent;

        if (options.craftedOverallRollStyle() == RollStyle.PERCENTAGE) {
            nameComponent.append(ColorScaleUtils.getPercentageTextComponent(
                    options.colorMap(),
                    item.getOverallPercentage(),
                    options.colorLerp(),
                    item.hasFullPerfectInternalRolls(),
                    options.decimalPlaces(),
                    true));
        } else if (options.craftedOverallRollStyle() == RollStyle.WHEEL) {
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

        lines.add(TooltipBuilder.divider(GearTier.CRAFTED));

        lines.addAll(RequirementsLineBuilder.buildRequirements(
                GearTier.CRAFTED, item.getRequirements(), item.getGearInstanceRequirements()));

        return lines;
    }

    private List<TooltipElement> buildIdentificationLines() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(GearTier.CRAFTED));

        lines.addAll(IdentificationLineBuilder.buildIdentificationLines(
                item.getIdentifications(),
                item.getRequirements(),
                true,
                Handlers.Tooltip.getTooltipOptions().craftedRollStyle()));

        return lines;
    }

    private List<TooltipElement> buildPowders() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(GearTier.CRAFTED));

        lines.addAll(PowderLineBuilder.buildPowders(item, GearTier.CRAFTED));

        return lines;
    }

    private List<TooltipElement> buildLore() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(GearTier.CRAFTED));

        lines.addAll(LoreLineBuilder.buildLore(item.getLore(), item.getCreator()));

        return lines;
    }

    private List<TooltipElement> buildStatsPage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();
        List<TooltipElement> requirements = buildRequirements();
        List<TooltipElement> identifications = buildIdentificationLines();
        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(requirements);
        lines.addAll(identifications);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildMiscStatsPage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();
        List<TooltipElement> powders = buildPowders();
        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(powders);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildLorePage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();
        List<TooltipElement> lore = buildLore();
        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(lore);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildPaginator() {
        List<TooltipElement> lines = new ArrayList<>();

        Component pageIcon = Component.literal(sourceType == ItemSourceType.ITEM ? "\uF002" : "\uE001")
                .withStyle(Style.EMPTY.withFont(
                        sourceType == ItemSourceType.ITEM
                                ? CommonFonts.CHAT_TILE_FONT
                                : CommonFonts.WYNNTILS_TOOLTIP_ICONS));
        lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, page, 3));
        lines.add(TooltipLine.empty());

        return lines;
    }

    private ItemEmblemSprite getItemEmblemSprite() {
        return switch (item.getGearType()) {
            case CHESTPLATE -> ItemEmblemSprite.CHESTPLATE;
            case LEGGINGS -> ItemEmblemSprite.LEGGINGS;
            case BOOTS -> ItemEmblemSprite.BOOTS;
            case BOW -> ItemEmblemSprite.BOW;
            case DAGGER -> ItemEmblemSprite.DAGGER;
            case WAND -> ItemEmblemSprite.WAND;
            case RELIK -> ItemEmblemSprite.RELIK;
            case SPEAR -> ItemEmblemSprite.SPEAR;
            case RING -> ItemEmblemSprite.RING;
            case BRACELET -> ItemEmblemSprite.BRACELET;
            case NECKLACE -> ItemEmblemSprite.NECKLACE;
            default -> ItemEmblemSprite.HELMET;
        };
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return GearTier.CRAFTED.getChatFormatting();
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines =
                switch (page) {
                    case 0 -> buildStatsPage();
                    case 1 -> buildMiscStatsPage();
                    case 2 -> buildLorePage();
                    default -> List.of();
                };

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}

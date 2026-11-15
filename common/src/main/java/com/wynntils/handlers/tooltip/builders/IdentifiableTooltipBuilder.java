/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.type.TooltipBannerIcon;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipBannerFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.HeaderStatsLineBuilder;
import com.wynntils.handlers.tooltip.components.IdentificationLineBuilder;
import com.wynntils.handlers.tooltip.components.LoreLineBuilder;
import com.wynntils.handlers.tooltip.components.PaginatorLineBuilder;
import com.wynntils.handlers.tooltip.components.PowderLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.components.SetBonusLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipOptions.RollStyle;
import com.wynntils.handlers.tooltip.type.TooltipOptions.WeightDisplay;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.models.items.properties.SetItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ColorScaleUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private static final Pattern ITEM_NAME_PATTERN = Pattern.compile(
            "§f\uDAFF\uDFF0.\uDAFF\uDFCF§#00eb1cff.§f\uDB00\uDC05§[5bcdef](?<fullName>(?<isShiny>Shiny )?(?<itemName>.+))");

    private final List<Component> parsedHeader;
    private final int page;

    private final IdentifiableItemProperty<T, U> item;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> item,
            ItemSourceType sourceType,
            String source,
            List<Component> parsedHeader,
            int page) {
        super(sourceType, source);

        this.item = item;
        this.parsedHeader = parsedHeader;
        this.page = page;
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem, ItemSourceType sourceType, String source, int page) {
        return new IdentifiableTooltipBuilder<>(identifiableItem, sourceType, source, null, page);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildFromItemStack(
            ItemStack itemStack, IdentifiableItemProperty<T, U> identifiableItem, int page) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        return new IdentifiableTooltipBuilder<>(
                identifiableItem, ItemSourceType.ITEM, "", extractHeader(tooltips), page);
    }

    private List<TooltipElement> buildNewHeader(String source) {
        List<TooltipElement> lines = new ArrayList<>();

        if (!source.isEmpty()) {
            lines.add(buildSourceLine());
        }

        lines.add(TooltipLine.empty());

        TooltipBannerIcon bannerIcon = getRestrictionIcon();
        Optional<String> setName = getSetName();
        List<Element> elements = getElements();
        Component elementTag = buildElementTag(elements);
        Optional<Component> tertiaryTag = Optional.empty();
        Optional<Component> quaternaryTag = Optional.empty();

        if (setName.isPresent()) {
            tertiaryTag = Optional.of(BannerBoxFont.buildMessage(
                    setName.get() + " set", getGearTier().getSecondaryColor(), CommonColors.BLACK, "\uDB00\uDC02"));

            if (!elements.isEmpty()) {
                quaternaryTag = Optional.of(elementTag);
            }
        } else if (!elements.isEmpty()) {
            tertiaryTag = Optional.of(elementTag);
        }

        String itemName = item.getName();
        if (getShinyStat().isPresent()) {
            itemName = "Shiny " + itemName;
        }

        Component nameComponent = buildItemName(
                item,
                itemName,
                Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(getRarityColor().asInt()));

        lines.addAll(buildHeader(
                getItemEmblem(),
                getItemEmblemSprite(),
                nameComponent,
                BannerBoxFont.buildMessage(getGearTier().getApiName(), getRarityColor(), CommonColors.BLACK, ""),
                BannerBoxFont.buildMessage(
                        getGearType().getModelKey(), getGearTier().getSecondaryColor(), CommonColors.BLACK, ""),
                bannerIcon,
                tertiaryTag,
                quaternaryTag));

        if (item.getItemInfo() instanceof GearInfo gearInfo) {
            lines.add(TooltipLine.empty());

            if (gearInfo.type().isWeapon()) {
                lines.add(HeaderStatsLineBuilder.buildDpsLine(
                        gearInfo.fixedStats().averageDps(), getGearTier()));

                if (gearInfo.fixedStats().attackSpeed().isPresent()) {
                    GearAttackSpeed attackSpeed =
                            gearInfo.fixedStats().attackSpeed().get();
                    lines.add(HeaderStatsLineBuilder.buildAttackSpeedLine(attackSpeed));
                }

                if (!gearInfo.fixedStats().damages().isEmpty()) {
                    lines.add(HeaderStatsLineBuilder.buildDamageGrid(
                            gearInfo.fixedStats().damages()));
                }
            } else {
                lines.add(HeaderStatsLineBuilder.buildHealthLine(
                        gearInfo.fixedStats().healthBuff(), getGearTier()));

                lines.addAll(HeaderStatsLineBuilder.buildDefenceLines(
                        gearInfo.fixedStats().defences()));
            }
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
                    boolean isShiny = matcher.group("isShiny") != null;
                    String matchedName = WynnUtils.normalizeBadString(matcher.group("itemName"));
                    String itemName = item.getName();
                    String displayName = isShiny ? "Shiny " : "";

                    if (itemName.equals(matchedName)) {
                        displayName += itemName;
                    } else if (isShiny && itemName.equals("Shiny " + matchedName)) {
                        displayName += matchedName;
                    } else if (itemName.equals("Ascended " + matchedName)) {
                        displayName += "Ascended " + matchedName;
                    } else {
                        displayName += matchedName;
                    }

                    Component nameComponent = buildItemName(
                            item, displayName, lastPart.getPartStyle().getStyle());

                    styledText = styledText
                            .replaceFirst(Pattern.quote(matcher.group("fullName")), "")
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

    private static Component buildItemName(IdentifiableItemProperty<?, ?> item, String itemName, Style style) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        MutableComponent nameComponent = Component.literal(itemName).withStyle(style);
        if (options.perfectTitle() && item.isPerfect()) {
            nameComponent = ComponentUtils.makeRainbowStyle("Perfect " + itemName, true)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT));
        } else if (options.defectiveTitle() && item.isDefective()) {
            nameComponent = ComponentUtils.makeCrimsonStyle("Defective " + itemName, true)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT));
        }

        boolean hasOverallValue = item.hasOverallValue();
        boolean isSpecialName = (item.isPerfect() || item.isDefective()) && !item.hasEstimatedOverallPercentage();
        boolean includeSpecialName = !isSpecialName || options.overallRollInSpecialName();

        if (hasOverallValue && includeSpecialName) {
            if (options.identifiedOverallRollStyle() == RollStyle.PERCENTAGE) {
                nameComponent.append(ColorScaleUtils.getPercentageTextComponent(
                        options.colorMap(),
                        item.getOverallPercentage(),
                        options.colorLerp(),
                        item.hasFullPerfectInternalRolls(),
                        options.decimalPlaces(),
                        item.hasEstimatedOverallPercentage()));
            } else if (options.identifiedOverallRollStyle() == RollStyle.WHEEL) {
                nameComponent
                        .append(Component.literal(" "))
                        .append(ColorScaleUtils.getWheelTextComponent(
                                options.colorMap(), item.getOverallPercentage(), options.colorLerp(), false));
            }
        }

        return nameComponent;
    }

    private List<TooltipElement> buildWeights() {
        List<TooltipElement> lines = new ArrayList<>();

        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        if (!(item instanceof UnknownGearItem)
                && item.getItemInstance().isPresent()
                && options.itemWeightSource() != ItemWeightSource.NONE) {
            List<ItemWeighting> noriWeightings =
                    Services.ItemWeight.getItemWeighting(item.getName(), ItemWeightSource.NORI);
            List<ItemWeighting> wynnpoolWeightings =
                    Services.ItemWeight.getItemWeighting(item.getName(), ItemWeightSource.WYNNPOOL);

            if (noriWeightings.isEmpty() && wynnpoolWeightings.isEmpty()) return lines;

            lines.add(divider(options.weightDisplay(), getGearTier()));

            boolean addNoriWeightings =
                    options.itemWeightSource() != ItemWeightSource.WYNNPOOL && !noriWeightings.isEmpty();
            boolean addWynnpoolWeightings =
                    options.itemWeightSource() != ItemWeightSource.NORI && !wynnpoolWeightings.isEmpty();

            if (addNoriWeightings) {
                lines.addAll(buildWeightLines(ItemWeightSource.NORI, options, noriWeightings));

                if (addWynnpoolWeightings) {
                    lines.add(TooltipLine.empty());
                }
            }

            if (addWynnpoolWeightings) {
                lines.addAll(buildWeightLines(ItemWeightSource.WYNNPOOL, options, wynnpoolWeightings));
            }
        }

        return lines;
    }

    private List<TooltipElement> buildWeightLines(
            ItemWeightSource weightSource, TooltipOptions options, List<ItemWeighting> weightings) {
        List<TooltipElement> lines = new ArrayList<>();

        Component sourceNameLine = Component.literal("\uE005")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS)
                        .withColor(weightSource.getColor().asInt()))
                .append(Component.literal(" " + EnumUtils.toNiceString(weightSource))
                        .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
        lines.add(TooltipLine.left(sourceNameLine));

        for (ItemWeighting weighting : weightings) {
            float percentage = Services.ItemWeight.calculateWeighting(weighting, item);
            MutableComponent percentageComponent = Component.empty();

            if (options.identifiedRollStyle() == RollStyle.PERCENTAGE) {
                percentageComponent.append(ColorScaleUtils.getPercentageTextComponent(
                                options.colorMap(), percentage, options.colorLerp(), false, options.decimalPlaces())
                        .withStyle(style -> style.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));
            } else if (options.identifiedRollStyle() == RollStyle.WHEEL) {
                percentageComponent.append(Component.literal(" ")
                        .append(ColorScaleUtils.getWheelTextComponent(
                                options.colorMap(), percentage, options.colorLerp(), false)));
            }

            lines.add(TooltipLine.leftRight(
                    Component.literal(weighting.weightName() + " Scale")
                            .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT),
                    percentageComponent));

            if (options.weightDisplay() != WeightDisplay.OVERALL) {
                lines.addAll(buildDetailedWeightLines(weighting, options));
            }
        }

        return lines;
    }

    private List<TooltipElement> buildDetailedWeightLines(ItemWeighting weighting, TooltipOptions options) {
        List<TooltipElement> lines = new ArrayList<>();

        Map<StatType, Pair<Float, Float>> statWeights = Services.ItemWeight.getStatWeights(weighting, item);
        statWeights.forEach((statType, weight) -> {
            String displayName = statType.getDisplayName() + " ";
            if (statType.getUnit() == StatUnit.RAW) {
                displayName += "Raw ";
            }

            float percentage = options.weightDisplay() == WeightDisplay.DISTRIBUTION
                    ? weight.b()
                    : (weight.a() / 100f) * weight.b();
            Component statWeight = Component.literal(new DecimalFormat("#.#").format(weight.a()) + "%")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(" " + displayName).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
            MutableComponent percentageComponent = Component.empty();

            if (options.identifiedRollStyle() == RollStyle.PERCENTAGE) {
                percentageComponent.append(ColorScaleUtils.getPercentageTextComponent(
                                options.colorMap(), percentage, options.colorLerp(), false, options.decimalPlaces())
                        .withStyle(style -> style.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));
            } else if (options.identifiedRollStyle() == RollStyle.WHEEL) {
                percentageComponent.append(Component.literal(" ")
                        .append(ColorScaleUtils.getWheelTextComponent(
                                options.colorMap(), percentage, options.colorLerp(), false)));
            }

            lines.add(TooltipLine.leftRight(statWeight, percentageComponent));
        });

        return lines;
    }

    private List<TooltipElement> buildRequirements() {
        List<TooltipElement> lines = new ArrayList<>();

        GearRequirements gearRequirements = getGearRequirements();
        GearInstanceRequirements instanceRequirements = getGearInstanceRequirements();

        if (gearRequirements != null) {
            lines.add(TooltipBuilder.divider(getGearTier()));

            lines.addAll(
                    RequirementsLineBuilder.buildRequirements(getGearTier(), gearRequirements, instanceRequirements));
        }

        return lines;
    }

    private List<TooltipElement> buildShinyStat() {
        List<TooltipElement> lines = new ArrayList<>();

        Optional<ShinyStat> shinyStatOpt = getShinyStat();
        if (shinyStatOpt.isPresent()) {
            lines.add(divider(getGearTier()));

            ShinyStat shinyStat = shinyStatOpt.get();

            Component shinyStatName = Component.literal("\uE04F")
                    .withStyle(CommonStyles.COMMON_WITHOUT_SHADOW)
                    .append(Component.literal(" " + shinyStat.statType().displayName())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(getDividerColor(getGearTier()).asInt())));

            MutableComponent shinyStatValue = Component.literal(String.format("%,d", shinyStat.value()))
                    .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT);

            if (shinyStat.shinyRerolls() > 1) {
                shinyStatValue
                        .append(Component.literal(" "))
                        .append(BannerBoxFont.buildMessage(
                                String.valueOf(shinyStat.shinyRerolls()),
                                getDividerColor(getGearTier()),
                                CommonColors.BLACK,
                                "\uDB00\uDC02\uDAFF\uDFFF"));
            }

            lines.add(TooltipLine.leftRight(shinyStatName, shinyStatValue));
        }

        return lines;
    }

    private List<TooltipElement> buildIdentificationLines() {
        List<TooltipElement> lines = new ArrayList<>();

        if (item.getPossibleValues().isEmpty() && item.getIdentifications().isEmpty()) return lines;

        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();
        IdentificationDisplay display = item.getItemInstance().isPresent() && item.hasOverallValue()
                ? options.identificationDisplay()
                : IdentificationDisplay.PERCENTAGE;

        lines.add(divider(display, getGearTier()));

        List<TooltipElement> notices = buildCalculationNotices(item, options, display);

        lines.addAll(notices);

        int rerolls = getRerolls();
        if (rerolls > 1) {
            Component rerollsLine = Component.empty()
                    .append(BannerBoxFont.buildMessage(
                            String.valueOf(rerolls),
                            getDividerColor(getGearTier()),
                            CommonColors.BLACK,
                            "\uDB00\uDC02\uDAFF\uDFFF"))
                    .append(TooltipBannerFont.buildMessage(
                            TooltipBannerIcon.REROLL, getDividerColor(getGearTier()), ""));
            lines.add(TooltipLine.right(rerollsLine));
        }

        lines.addAll(IdentificationLineBuilder.buildIdentificationLines(
                item.getIdentifications(),
                item.getPossibleValues(),
                !item.isUnidentified(),
                getGearRequirements(),
                item.getIdentificationLevelRange(),
                item instanceof GearTypeItemProperty gear && gear.getGearType().isWeapon(),
                display,
                options.identifiedRollStyle()));

        return lines;
    }

    private static List<TooltipElement> buildCalculationNotices(
            IdentifiableItemProperty<?, ?> item, TooltipOptions options, IdentificationDisplay display) {
        if (!item.hasEstimatedOverallPercentage()) return List.of();

        List<TooltipElement> lines = new ArrayList<>();
        boolean showsEstimatedPercentage = options.identifiedRollStyle() == RollStyle.PERCENTAGE
                || (item.hasOverallValue() && options.identifiedOverallRollStyle() == RollStyle.PERCENTAGE);

        if (display == IdentificationDisplay.PERCENTAGE && showsEstimatedPercentage) {
            lines.add(TooltipWrappedText.center(Component.literal("Percentages marked with ~ are estimates")
                    .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)));
        }

        if (display != IdentificationDisplay.PERCENTAGE) {
            lines.add(TooltipWrappedText.center(
                    Component.literal(EnumUtils.toNiceString(display) + " could not be calculated for some lines")
                            .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)));
        }

        return lines;
    }

    private List<TooltipElement> buildMajorId(boolean hasIdentifications) {
        List<TooltipElement> lines = new ArrayList<>();

        if (item instanceof UnknownGearItem unknown
                && !unknown.getMajorIdLines().isEmpty()) {
            if (hasIdentifications) {
                lines.add(TooltipLine.empty());
            }

            unknown.getMajorIdLines().forEach(line -> lines.add(TooltipWrappedText.left(line)));
            return lines;
        }

        if (item.getItemInfo() instanceof GearInfo gearInfo
                && gearInfo.fixedStats().majorIds().isPresent()) {
            if (hasIdentifications) {
                lines.add(TooltipLine.empty());
            }

            GearMajorId gearMajorId = gearInfo.fixedStats().majorIds().get();
            MutableComponent majorIdLine = Component.empty()
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(getDividerColor(getGearTier()).asInt()))
                    .append(Component.literal("\uE000")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_IDENTIFICATION_MAJOR_FONT)))
                    .append(Component.literal("\uDB00\uDC02"))
                    .append(Component.literal(gearMajorId.name() + ": "))
                    .append(gearMajorId.lore().getComponent());
            lines.add(TooltipWrappedText.left(majorIdLine));
        }

        return lines;
    }

    private List<TooltipElement> buildRestriction() {
        List<TooltipElement> lines = new ArrayList<>();

        GearRestrictions restrictions = getGearRestriction();

        if (restrictions != null && restrictions != GearRestrictions.NONE) {
            lines.add(divider(getGearTier()));

            MutableComponent restrictionHeader = Component.empty()
                    .append(TooltipBannerFont.buildMessage(
                            getRestrictionIcon(), RESTRICTION_BACKGROUND_COLOR, "\uDAFF\uDFFF"))
                    .append(BannerBoxFont.buildMessage(
                            restrictions.getName(), RESTRICTION_BACKGROUND_COLOR, CommonColors.BLACK, ""));

            lines.add(TooltipLine.left(restrictionHeader));

            Component restrictionDescription =
                    Component.literal(restrictions.getDescription()).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT);

            lines.add(TooltipWrappedText.left(restrictionDescription));
        }

        return lines;
    }

    private List<TooltipElement> buildSetBonuses() {
        List<TooltipElement> lines = new ArrayList<>();

        if (!(item instanceof SetItemProperty setItem)) return lines;

        Optional<SetInfo> setInfoOpt = setItem.getSetInfo();
        if (setInfoOpt.isEmpty() && sourceType != ItemSourceType.ITEM) return lines;

        SetInfo setInfo = setInfoOpt.orElse(null);
        int equippedCount;
        int totalCount;
        Map<StatType, Integer> activeBonuses;

        if (sourceType == ItemSourceType.ITEM) {
            Optional<SetInstance> setInstanceOpt = setItem.getSetInstance();
            if (setInstanceOpt.isEmpty()) return lines;

            SetInstance setInstance = setInstanceOpt.get();
            equippedCount = setInstance.equippedCount();
            totalCount = setInstance.totalCount();
            activeBonuses = setInstance.bonuses();
        } else {
            equippedCount = 0;
            totalCount = setInfo.items().size();
            activeBonuses = Map.of();
        }

        lines.add(divider(getGearTier()));

        String setName = setInfo != null
                ? setInfo.cleanName()
                : setItem.getSetInstance()
                        .map(SetInstance::name)
                        .filter(name -> !name.isEmpty())
                        .orElse("unknown");
        lines.add(SetBonusLineBuilder.buildHeader(setName, getGearTier(), equippedCount, totalCount));

        lines.add(TooltipLine.empty());

        if (equippedCount > 0) {
            lines.addAll(SetBonusLineBuilder.buildBonuses(
                    activeBonuses, getGearRequirements(), item.getIdentificationLevelRange()));
        } else {
            Component equipItems = Component.literal("Equip more pieces of this set to unlock its bonus")
                    .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT);
            lines.add(TooltipWrappedText.center(equipItems, 140));
        }

        if (sourceType == ItemSourceType.ITEM) {
            List<Component> setMajorIdLines =
                    setItem.getSetInstance().map(SetInstance::majorIdLines).orElse(List.of());
            if (!setMajorIdLines.isEmpty()) {
                lines.add(TooltipLine.empty());
                setMajorIdLines.forEach(line -> lines.add(TooltipWrappedText.left(line)));
            }
        }

        lines.add(TooltipLine.empty());

        return lines;
    }

    private List<TooltipElement> buildPowders() {
        List<TooltipElement> lines = new ArrayList<>();

        // Charms do not implement PowderedItemProperty as they don't support powders but they still include
        // the powder segment in the tooltip
        if (getGearType() == GearType.CHARM) {
            lines.add(divider(getGearTier()));
            lines.addAll(PowderLineBuilder.buildPowders(0, List.of(), getGearTier(), Optional.empty()));
        } else if (item instanceof PowderedItemProperty powderedItem) {
            lines.add(divider(getGearTier()));

            if (sourceType == ItemSourceType.GUIDE) {
                lines.addAll(PowderLineBuilder.buildPowders(
                        powderedItem.getPowderSlots(), List.of(), getGearTier(), Optional.empty()));
            } else {
                lines.addAll(PowderLineBuilder.buildPowders(
                        powderedItem.getPowderSlots(),
                        powderedItem.getPowders(),
                        getGearTier(),
                        powderedItem.getPowderSpecial()));
            }
        }

        return lines;
    }

    private List<TooltipElement> buildLore() {
        List<TooltipElement> lines = new ArrayList<>();

        // Tomes don't have lore
        if (item.getItemInfo() instanceof TomeInfo) return lines;

        Optional<StyledText> knownLore =
                switch (item.getItemInfo()) {
                    case GearInfo info -> info.metaInfo().lore();
                    case TomeInfo info -> info.metaInfo().lore();
                    case CharmInfo info -> info.metaInfo().lore();
                    default -> Optional.empty();
                };
        StyledText parsedLore = item instanceof PagedItemProperty paged ? paged.getLore() : StyledText.EMPTY;

        lines.add(divider(getGearTier()));
        lines.addAll(LoreLineBuilder.buildLore(knownLore.orElse(parsedLore), ""));

        return lines;
    }

    private List<TooltipElement> buildObtainInfo() {
        List<TooltipElement> lines = new ArrayList<>();

        // We only show obtain info in guides
        if (sourceType != ItemSourceType.GUIDE) return lines;

        List<ItemObtainInfo> itemObtainInfos = getObtainInfo();

        if (!itemObtainInfos.isEmpty()) {
            lines.add(divider(getGearTier()));

            lines.addAll(buildObtainInfo(itemObtainInfos, TooltipAlignment.CENTER));
        }

        return lines;
    }

    private List<TooltipElement> buildStatsPage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();

        List<TooltipElement> weights = buildWeights();
        List<TooltipElement> requirements = buildRequirements();
        List<TooltipElement> shinyStat = buildShinyStat();
        List<TooltipElement> identifications = buildIdentificationLines();
        List<TooltipElement> majorId = buildMajorId(!identifications.isEmpty());

        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(weights);
        lines.addAll(requirements);
        lines.addAll(shinyStat);
        lines.addAll(identifications);
        lines.addAll(majorId);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildMiscStatsPage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();

        List<TooltipElement> restriction = buildRestriction();
        List<TooltipElement> setBonuses = buildSetBonuses();
        List<TooltipElement> powders = buildPowders();

        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(restriction);
        lines.addAll(setBonuses);
        lines.addAll(powders);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildLorePage() {
        List<TooltipElement> header = parsedHeader == null ? buildNewHeader(source) : decorateHeader();

        List<TooltipElement> lore = buildLore();
        List<TooltipElement> obtainInfo = buildObtainInfo();

        List<TooltipElement> paginator = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(lore);
        lines.addAll(obtainInfo);
        lines.addAll(paginator);

        return lines;
    }

    private List<TooltipElement> buildPaginator() {
        List<TooltipElement> lines = new ArrayList<>();

        boolean isTome = getGearType() == GearType.MASTERY_TOME;

        if (sourceType == ItemSourceType.ITEM) {
            if (item instanceof PagedItemProperty && !isTome) {
                Component pageIcon =
                        Component.literal("\uF002").withStyle(Style.EMPTY.withFont(CommonFonts.CHAT_TILE_FONT));
                lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, page, 3));
                lines.add(TooltipLine.empty());
            } else {
                lines.add(TooltipLine.empty());
            }
        } else if (sourceType == ItemSourceType.GUIDE) {
            Component pageIcon =
                    Component.literal("\uE001").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS));
            int displayedPage = isTome && page == 2 ? 1 : page;
            lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, displayedPage, isTome ? 2 : 3));
            lines.add(TooltipLine.empty());
        } else if (sourceType == ItemSourceType.ENCODED) {
            if (!isTome) {
                Component pageIcon =
                        Component.literal("\uE001").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS));
                lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, page, 3));
                lines.add(TooltipLine.empty());
            } else {
                lines.add(TooltipLine.empty());
            }
        }

        return lines;
    }

    private ItemEmblem getItemEmblem() {
        return switch (item.getItemInfo()) {
            case GearInfo info -> info.emblem();
            case CharmInfo info -> info.emblem();
            case TomeInfo info -> info.emblem();
            default -> new ItemEmblem(ItemEmblem.ItemEmblemShape.DIAMOND, 1);
        };
    }

    private GearType getGearType() {
        return switch (item.getItemInfo()) {
            case UnknownGearItem unknown -> unknown.getGearType();
            case GearInfo info -> info.type();
            case CharmInfo info -> GearType.CHARM;
            case TomeInfo info -> GearType.MASTERY_TOME;
            default -> GearType.CHESTPLATE;
        };
    }

    private ItemEmblemSprite getItemEmblemSprite() {
        return switch (getGearType()) {
            case CHARM -> ItemEmblemSprite.CHARM;
            case MASTERY_TOME -> ItemEmblemSprite.TOME;
            default -> getGearEmblemSprite(getGearType());
        };
    }

    private ItemEmblemSprite getGearEmblemSprite(GearType gearType) {
        return switch (gearType) {
            case HELMET -> ItemEmblemSprite.HELMET;
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
            default -> ItemEmblemSprite.CHESTPLATE;
        };
    }

    private GearTier getGearTier() {
        return switch (item.getItemInfo()) {
            case UnknownGearItem unknown -> unknown.getGearTier();
            case GearInfo info -> info.tier();
            case CharmInfo info -> info.tier();
            case TomeInfo info -> info.tier();
            default -> GearTier.NORMAL;
        };
    }

    private CustomColor getRarityColor() {
        return CustomColor.fromChatFormatting(getGearTier().getChatFormatting());
    }

    private TooltipBannerIcon getRestrictionIcon() {
        GearRestrictions gearRestrictions = getGearRestriction();

        if (gearRestrictions == null || gearRestrictions == GearRestrictions.NONE) {
            return null;
        }

        return gearRestrictions == GearRestrictions.UNTRADABLE ? TooltipBannerIcon.UNTRADABLE : TooltipBannerIcon.QUEST;
    }

    private GearRestrictions getGearRestriction() {
        return switch (item.getItemInfo()) {
            case UnknownGearItem unknown -> unknown.getRestrictions();
            case GearInfo info -> info.metaInfo().restrictions();
            case CharmInfo info -> info.metaInfo().restrictions();
            case TomeInfo info -> info.metaInfo().restrictions();
            default -> null;
        };
    }

    private Optional<String> getSetName() {
        if (item.getItemInfo() instanceof GearInfo gearInfo
                && gearInfo.setInfo().isPresent()) {
            return Optional.ofNullable(gearInfo.setInfo().get().cleanName());
        }

        return Optional.empty();
    }

    private List<Element> getElements() {
        if (item.getItemInfo() instanceof GearInfo gearInfo) {
            return gearInfo.elements();
        }

        return List.of();
    }

    private Component buildElementTag(List<Element> elements) {
        MutableComponent elementTag = Component.empty();

        if (elements.size() == 1) {
            elementTag.append(TooltipBannerFont.buildMessage(
                    getElementIcon(elements.getFirst()), getGearTier().getSecondaryColor(), "\uDAFF\uDFFF"));
            elementTag.append(BannerBoxFont.buildMessage(
                    elements.getFirst().getDisplayName(),
                    getGearTier().getSecondaryColor(),
                    CommonColors.BLACK,
                    "\uDB00\uDC02"));
        } else {
            for (Element element : elements) {
                elementTag.append(TooltipBannerFont.buildMessage(
                        getElementIcon(element),
                        getGearTier().getSecondaryColor(),
                        element == elements.getLast() ? "" : "\uDAFF\uDFFF"));
            }
        }

        return elementTag;
    }

    private TooltipBannerIcon getElementIcon(Element element) {
        return switch (element) {
            case EARTH -> TooltipBannerIcon.EARTH;
            case THUNDER -> TooltipBannerIcon.THUNDER;
            case WATER -> TooltipBannerIcon.WATER;
            case FIRE -> TooltipBannerIcon.FIRE;
            case AIR -> TooltipBannerIcon.AIR;
        };
    }

    private GearRequirements getGearRequirements() {
        return switch (item) {
            case GearItem gearItem -> gearItem.getItemInfo().requirements();
            case UnknownGearItem unknownGearItem -> unknownGearItem.getRequirements();
            case CharmItem charmItem -> charmItem.getItemInfo().requirements();
            case TomeItem tomeItem -> tomeItem.getItemInfo().requirements();
            default -> null;
        };
    }

    private GearInstanceRequirements getGearInstanceRequirements() {
        return switch (item) {
            case GearItem gearItem -> gearItem.getGearInstanceRequirements();
            case UnknownGearItem unknownGearItem -> unknownGearItem.getGearInstanceRequirements();
            case CharmItem charmItem -> charmItem.getGearInstanceRequirements();
            case TomeItem tomeItem -> tomeItem.getGearInstanceRequirements();
            default -> null;
        };
    }

    private Optional<ShinyStat> getShinyStat() {
        if (item instanceof ShinyItemProperty shinyItem) {
            return shinyItem.getShinyStat();
        }

        return Optional.empty();
    }

    private int getRerolls() {
        if (item instanceof UnknownGearItem unknown) return unknown.getRerollCount();
        if (item.getItemInstance().isEmpty()) return 0;

        return switch (item.getItemInstance().get()) {
            case GearInstance gearInstance -> gearInstance.rerolls();
            case CharmInstance charmInstance -> charmInstance.rerolls();
            case TomeInstance tomeInstance -> tomeInstance.rerolls();
            default -> 0;
        };
    }

    private List<ItemObtainInfo> getObtainInfo() {
        return switch (item.getItemInfo()) {
            case GearInfo info -> Models.Gear.getObtainInfo(info);
            case CharmInfo info -> Models.Rewards.getCharmObtainInfo(info);
            case TomeInfo info -> Models.Rewards.getTomeObtainInfo(info);
            default -> List.of();
        };
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return getGearTier().getChatFormatting();
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

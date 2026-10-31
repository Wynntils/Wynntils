/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.type.TooltipBannerIcon;
import com.wynntils.core.text.fonts.wynnfonts.TooltipBannerFont;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipOptions.WeightDisplay;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.activities.quests.SimpleQuestInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public abstract class TooltipBuilder {
    protected static final CustomColor RESTRICTION_BACKGROUND_COLOR = CustomColor.fromInt(0xff4242);
    private static final CustomColor NORMAL_DIVIDER_COLOR = CustomColor.fromInt(0xe0e0e0);
    private static final CustomColor UNIQUE_DIVIDER_COLOR = CustomColor.fromInt(0xfff2b3);
    private static final CustomColor RARE_DIVIDER_COLOR = CustomColor.fromInt(0xf2c2f2);
    private static final CustomColor LEGENDARY_DIVIDER_COLOR = CustomColor.fromInt(0xc2f2f2);
    private static final CustomColor FABLED_DIVIDER_COLOR = CustomColor.fromInt(0xf2c2c2);
    private static final CustomColor MYTHIC_DIVIDER_COLOR = CustomColor.fromInt(0xe0b3e6);
    private static final CustomColor CRAFTED_DIVIDER_COLOR = CustomColor.fromInt(0xe0b3e6);
    private static final CustomColor DEFAULT_DIVIDER_COLOR = CustomColor.fromInt(0xaed4d4);

    protected final ItemSourceType sourceType;
    protected final String source;

    private ClassType cachedCurrentClass;
    private TooltipOptions cachedTooltipOptions;

    protected List<Component> tooltipLines;

    protected TooltipBuilder(ItemSourceType sourceType, String source) {
        this.sourceType = sourceType;
        this.source = source;
    }

    public List<Component> getTooltipLines(ClassType currentClass) {
        if (currentClass != cachedCurrentClass
                || !Objects.equals(cachedTooltipOptions, Handlers.Tooltip.getTooltipOptions())) {
            cachedCurrentClass = currentClass;
            cachedTooltipOptions = Handlers.Tooltip.getTooltipOptions();
            buildTooltip();
        }

        return Collections.unmodifiableList(tooltipLines);
    }

    public static TooltipElement divider(GearTier tier) {
        return divider(IdentificationDisplay.PERCENTAGE, getDividerColor(tier));
    }

    public static TooltipElement divider(CustomColor color) {
        return divider(IdentificationDisplay.PERCENTAGE, color);
    }

    public static TooltipElement divider(IdentificationDisplay display, GearTier tier) {
        return divider(display, getDividerColor(tier));
    }

    public static TooltipElement divider(WeightDisplay display, GearTier tier) {
        return divider(display, getDividerColor(tier));
    }

    public static TooltipElement divider(WeightDisplay display, CustomColor color) {
        return divider(display == WeightDisplay.OVERALL ? "" : EnumUtils.toNiceString(display), color);
    }

    public static TooltipElement divider(IdentificationDisplay display, CustomColor color) {
        return divider(display == IdentificationDisplay.PERCENTAGE ? "" : EnumUtils.toNiceString(display), color);
    }

    public static TooltipElement divider(String title, CustomColor color) {
        MutableComponent divider = Component.empty().withColor(color.asInt()).withoutShadow();
        if (title.isEmpty()) {
            divider.append(
                    Component.literal("\uE000").withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_DIVIDER_FONT)));
        } else {
            divider.append(Component.literal("\uE000")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_DIVIDER_FONT)))
                    .append(Component.literal(" " + title + " ")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)))
                    .append(Component.literal("\uE001")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_DIVIDER_FONT)));
        }

        return TooltipLine.center(divider);
    }

    public static CustomColor getDividerColor(GearTier tier) {
        return switch (tier) {
            case NORMAL -> NORMAL_DIVIDER_COLOR;
            case UNIQUE -> UNIQUE_DIVIDER_COLOR;
            case RARE -> RARE_DIVIDER_COLOR;
            case LEGENDARY -> LEGENDARY_DIVIDER_COLOR;
            case FABLED -> FABLED_DIVIDER_COLOR;
            case MYTHIC -> MYTHIC_DIVIDER_COLOR;
            case CRAFTED -> CRAFTED_DIVIDER_COLOR;
            default -> DEFAULT_DIVIDER_COLOR;
        };
    }

    public static Component getNonContributingSkillPrefix(Skill skill) {
        String prefix =
                switch (skill) {
                    case STRENGTH -> "\uDAFF\uDFFF\uE010\uDB00\uDC02";
                    case DEXTERITY -> "\uE011\uDB00\uDC02";
                    case INTELLIGENCE -> "\uDAFF\uDFFF\uE012\uDB00\uDC02";
                    case DEFENCE -> "\uDAFF\uDFFF\uE013\uDB00\uDC01\uDB00\uDC02";
                    case AGILITY -> "\uE014\uDB00\uDC02";
                };

        return Component.literal(prefix)
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                        .withoutShadow());
    }

    protected List<TooltipElement> buildHeader(
            ItemEmblem emblem,
            ItemEmblemSprite sprite,
            Component itemName,
            Component primaryTag,
            Component secondaryTag,
            TooltipBannerIcon bannerIcon,
            Optional<Component> tertiaryTag,
            Optional<Component> quaternaryTag) {
        List<TooltipElement> headerLines = new ArrayList<>();

        MutableComponent emblemLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFF0").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(emblem.getFrameCode(tertiaryTag.isPresent()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_FRAME_FONT))
                        .withoutShadow())
                .append(Component.literal("\uDAFF\uDFCF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(sprite.getSpriteCode(tertiaryTag.isPresent()))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(itemName);
        headerLines.add(TooltipLine.left(emblemLine));

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(primaryTag)
                .append(Component.literal("\uDB00\uDC03").withStyle(CommonStyles.SPACE))
                .append(secondaryTag)
                .append(Component.literal("\uDB00\uDC03").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));

        if (bannerIcon != null) {
            tagLine.append(Component.literal("\uDB00\uDC02").withStyle(CommonStyles.SPACE));

            tagLine.append(TooltipBannerFont.buildMessage(bannerIcon, RESTRICTION_BACKGROUND_COLOR, ""));
        }

        headerLines.add(TooltipLine.left(tagLine));

        if (tertiaryTag.isPresent()) {
            MutableComponent secondTagLine = Component.empty()
                    .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                    .append(tertiaryTag.get());

            quaternaryTag.ifPresent(tag -> secondTagLine
                    .append(Component.literal("\uDB00\uDC01").withStyle(CommonStyles.SPACE))
                    .append(tag));

            headerLines.add(TooltipLine.left(secondTagLine));
        }

        return headerLines;
    }

    protected List<TooltipElement> buildHeader(
            ItemEmblem emblem,
            ItemEmblemSprite sprite,
            Component itemName,
            Component primaryTag,
            Component secondaryTag) {
        return buildHeader(
                emblem, sprite, itemName, primaryTag, secondaryTag, null, Optional.empty(), Optional.empty());
    }

    protected static List<TooltipElement> buildObtainInfo(
            List<ItemObtainInfo> obtainInfos, TooltipAlignment alignment) {
        Map<ItemObtainType, List<ItemObtainInfo>> groupedObtainInfos = new LinkedHashMap<>();
        for (ItemObtainInfo obtainInfo : obtainInfos) {
            groupedObtainInfos
                    .computeIfAbsent(obtainInfo.sourceType(), type -> new ArrayList<>())
                    .add(obtainInfo);
        }

        List<TooltipElement> lines = new ArrayList<>();
        for (Map.Entry<ItemObtainType, List<ItemObtainInfo>> entry : groupedObtainInfos.entrySet()) {
            ItemObtainType type = entry.getKey();
            Component description;
            if (type == ItemObtainType.UNAVAILABLE) {
                description = Component.literal("This item is no longer available")
                        .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT);
            } else if (type == ItemObtainType.UNKNOWN) {
                description = Component.literal("There is no known obtain method for this item")
                        .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT);
            } else {
                List<String> sources = entry.getValue().stream()
                        .map(TooltipBuilder::buildObtainDescription)
                        .distinct()
                        .toList();
                String joinedSources = sources.size() == 1
                        ? sources.getFirst()
                        : String.join(", ", sources.subList(0, sources.size() - 1)) + " and " + sources.getLast();
                String prefix = type == ItemObtainType.INTERACTION
                                && entry.getValue().stream()
                                        .anyMatch(info -> info.name().isPresent())
                        ? "This item can be obtained by interacting with "
                        : "This item can be obtained from ";
                description = Component.literal(prefix)
                        .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                        .append(Component.literal(joinedSources).withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
            }

            lines.add(new TooltipWrappedText(description, alignment));
        }

        return lines;
    }

    private static String buildObtainDescription(ItemObtainInfo obtainInfo) {
        ItemObtainType type = obtainInfo.sourceType();
        String description;
        if (obtainInfo.name().isEmpty() || type == ItemObtainType.GUILD) {
            description = type.getDisplayName();
        } else {
            String name = obtainInfo.name().get();

            description = switch (type) {
                case LOOTRUN ->
                    name.equals("Lootrun Rewards")
                            ? type.getDisplayName()
                            : "the " + name + " " + type.getDisplayName();
                case QUEST -> {
                    SimpleQuestInfo questInfo = Models.Quest.getQuestInfo(name);
                    String questName = questInfo == SimpleQuestInfo.UNKNOWN ? name : questInfo.displayName();
                    yield "the " + questName + " " + type.getDisplayName();
                }
                case CAVE_LOOT_CHEST, GATHERING, LOOT_CHEST, NORMAL_MOB_DROP -> type.getDisplayName() + " " + name;
                case ENVIRONMENT, TINKERING -> "the " + name;
                case INTERACTION, SPECIFIC_MOB_DROP -> name;
                case MOB_REGION -> "mobs around " + name;
                case RARE_MOB_REGION -> "rare mobs around " + name;
                default -> "the " + name + " " + type.getDisplayName();
            };
        }

        return description;
    }

    protected TooltipElement buildSourceLine() {
        Component sourceLine = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .applyFormat(getSourceColor()))
                .append(Component.literal("\uE004").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS)))
                .append(Component.literal("\uDB00\uDC02"))
                .append(Component.literal(source)
                        .withStyle(style -> style.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .applyFormat(ChatFormatting.WHITE)));

        return TooltipLine.left(sourceLine);
    }

    protected ChatFormatting getSourceColor() {
        return ChatFormatting.WHITE;
    }

    protected static List<Component> extractHeader(List<Component> tooltips) {
        List<Component> header = new ArrayList<>();

        for (Component line : tooltips) {
            StyledTextPart lastPart = StyledText.fromComponent(line).getLastPart();
            if (lastPart != null && lastPart.getPartStyle().getFont().equals(CommonFonts.TOOLTIP_DIVIDER_FONT)) {
                break;
            }

            header.add(line);
        }

        return header;
    }

    protected abstract void buildTooltip();
}

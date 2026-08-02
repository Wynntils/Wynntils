/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gatheringtool;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGatheringToolItemStack extends GuideItemStack {
    private static final FontDescription IDENTIFICATION_METER =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));
    private static final String DEFAULT_SPACE = "\uDAFF\uDFA6\uDB00\uDC66";

    private List<Component> generatedTooltip;
    private final GatheringToolInfo toolInfo;

    public GuideGatheringToolItemStack(GatheringToolInfo toolInfo) {
        super(toolInfo.material().itemStack(), new GatheringToolItem(toolInfo, CappedValue.EMPTY), toolInfo.name());

        this.toolInfo = toolInfo;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    private List<Component> generateLore() {
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.empty());

        MutableComponent emblemLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFF0").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE031")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_FRAME_FONT))
                        .withoutShadow())
                .append(Component.literal("\uDAFF\uDFCF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(toolInfo.gatheringToolType().getEmblemCharacter())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(toolInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(CustomColor.fromChatFormatting(
                                                toolInfo.gearTier().getChatFormatting())
                                        .asInt())));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerBoxFont.buildMessage(
                        toolInfo.gearTier().getName(),
                        CustomColor.fromChatFormatting(toolInfo.gearTier().getChatFormatting()),
                        CommonColors.BLACK,
                        "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage(
                        EnumUtils.toNiceString(toolInfo.gatheringToolType()),
                        toolInfo.gearTier().getSecondaryColor(),
                        CommonColors.BLACK,
                        ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);
        tooltipLines.add(Component.empty());

        MutableComponent gatheringSpeedLine = Component.empty()
                .append(Component.literal(String.valueOf(toolInfo.gatheringSpeed()))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.OFFSET_QUAD_12)
                                .withColor(
                                        toolInfo.gearTier().getSecondaryColor().asInt())))
                .append(Component.literal(" Gathering Speed")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))
                        .withStyle(ChatFormatting.WHITE));
        tooltipLines.add(gatheringSpeedLine);

        MutableComponent durabilityLine = Component.empty()
                .append(Component.literal("\uE023\uDAFF\uDFF7")
                        .withStyle(Style.EMPTY.withFont(IDENTIFICATION_METER).withColor(ChatFormatting.DARK_GRAY))
                        .withoutShadow())
                .append(Component.literal("\uE023")
                        .withStyle(Style.EMPTY
                                .withFont(IDENTIFICATION_METER)
                                .withColor(
                                        toolInfo.gearTier().getSecondaryColor().asInt()))
                        .withoutShadow())
                .append(Component.literal(" Durability " + toolInfo.durability() + "/" + toolInfo.durability())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        tooltipLines.add(durabilityLine);

        ProfessionType professionType = toolInfo.professionType();
        int level = toolInfo.level();
        MutableComponent levelHeader = Component.empty()
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal((Models.Profession.getLevel(professionType) >= level ? "\uE006" : "\uE007")
                                + "\uDAFF\uDFFF")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT))
                        .withoutShadow())
                .append(Component.literal(" " + professionType.getDisplayName() + " Level")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));

        Pair<Component, Component> levelPair = Pair.of(
                levelHeader,
                Component.literal(String.valueOf(level))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));

        Component rawLevelPair = Component.empty().append(levelPair.a()).append(levelPair.b());
        tooltipLines.add(rawLevelPair);

        int widestLine = tooltipLines.stream()
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);
        int currentWidth = McUtils.mc().font.width(rawLevelPair);
        String space = Managers.Font.calculateOffset(currentWidth, widestLine);
        if (space.isEmpty()) {
            space = DEFAULT_SPACE;
        }

        tooltipLines.set(
                tooltipLines.size() - 1,
                Component.empty()
                        .append(levelPair.a())
                        .append(Component.literal(space).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                        .append(levelPair.b()));

        if (space.equals(DEFAULT_SPACE)) {
            widestLine = McUtils.mc().font.width(tooltipLines.getLast());
        }

        Component divider = Component.literal("\uE000")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.TOOLTIP_DIVIDER_FONT)
                        .withColor(toolInfo.gearTier().getSecondaryColor().asInt()));
        int dividerWidth = McUtils.mc().font.width(divider);
        int target = dividerWidth + ((widestLine - dividerWidth) / 2);
        String spacing = Managers.Font.calculateOffset(dividerWidth, target);
        Component centeredDivider = Component.empty()
                .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(divider);
        tooltipLines.add(6, centeredDivider);

        return tooltipLines;
    }

    public GatheringToolInfo getGatheringToolInfo() {
        return toolInfo;
    }
}

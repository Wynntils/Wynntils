/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.material;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerSymbolFont;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideMaterialItemStack extends GuideItemStack {
    private static final CustomColor MATERIAL_NAME_COLOR = CustomColor.fromInt(0x3cb0e6);
    private static final CustomColor MATERIAL_INFO_COLOR = CustomColor.fromInt(0x90c5de);
    private static final int USAGE_WIDTH = 175;

    private final MaterialInfo materialInfo;
    private final int tier;
    private List<Component> generatedTooltip;

    public GuideMaterialItemStack(MaterialInfo materialInfo, int tier) {
        super(materialInfo.material().itemStack(), new MaterialItem(materialInfo, tier), materialInfo.name());

        this.materialInfo = materialInfo;
        this.tier = tier;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public int getTier() {
        return tier;
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
                .append(Component.literal("\uE017")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(materialInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(MATERIAL_NAME_COLOR.asInt())));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerSymbolFont.buildMessage(
                        3,
                        tier,
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(getColorForTier(tier)),
                        CommonColors.BLACK,
                        "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage("material", MATERIAL_INFO_COLOR, CommonColors.BLACK, ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);

        ProfessionType professionType = materialInfo.professionType();
        int level = materialInfo.level();
        MutableComponent levelHeader = Component.empty()
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal((Models.Profession.getLevel(professionType) >= level ? "\uE006" : "\uE007")
                                + "\uDAFF\uDFFF")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT))
                        .withoutShadow())
                .append(Component.literal(" "))
                .append(Component.literal(getIconPrefix(professionType))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT)))
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

        StyledText[] splitLore = materialInfo.lore().split("\n\n");
        StyledText mountUsage = splitLore[0];
        StyledText otherUsage = splitLore[1];

        List<Component> splitMountUsage = ComponentUtils.splitComponent(mountUsage.getComponent(), USAGE_WIDTH);
        List<Component> splitOtherUsage = ComponentUtils.splitComponent(otherUsage.getComponent(), USAGE_WIDTH);

        tooltipLines.addAll(splitMountUsage);
        tooltipLines.add(Component.empty());
        tooltipLines.addAll(splitOtherUsage);

        int widestLine = tooltipLines.stream()
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);
        int currentWidth = McUtils.mc().font.width(rawLevelPair);
        String space = Managers.Font.calculateOffset(currentWidth, widestLine);

        tooltipLines.set(
                3,
                Component.empty()
                        .append(levelPair.a())
                        .append(Component.literal(space).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                        .append(levelPair.b()));

        // Now that we have the widest line, reinsert the usage lines but with padding to make them centered
        for (int i = 4; i < tooltipLines.size(); i++) {
            int lineWidth = McUtils.mc().font.width(tooltipLines.get(i));
            int target = lineWidth + ((widestLine - lineWidth) / 2);
            String spacing = Managers.Font.calculateOffset(lineWidth, target);
            Component centeredLine = Component.empty()
                    .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                    .append(tooltipLines.get(i));
            tooltipLines.set(i, centeredLine);
        }

        Component divider = Component.literal("\uE000")
                .withStyle(
                        Style.EMPTY.withFont(CommonFonts.TOOLTIP_DIVIDER_FONT).withColor(MATERIAL_INFO_COLOR.asInt()));
        int dividerWidth = McUtils.mc().font.width(divider);
        int target = dividerWidth + ((widestLine - dividerWidth) / 2);
        String spacing = Managers.Font.calculateOffset(dividerWidth, target);
        Component centeredDivider = Component.empty()
                .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(divider);
        tooltipLines.add(3, centeredDivider);
        tooltipLines.add(5, centeredDivider);

        return tooltipLines;
    }

    private ChatFormatting getColorForTier(Integer tier) {
        return switch (tier) {
            case 3 -> ChatFormatting.AQUA;
            case 2 -> ChatFormatting.LIGHT_PURPLE;
            case 1 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.DARK_GRAY;
        };
    }

    private static String getIconPrefix(ProfessionType professionType) {
        return switch (professionType) {
            case FARMING -> "\uE000";
            case FISHING -> "\uE001\uDAFF\uDFFE";
            case MINING -> "\uE002\uDAFF\uDFFE";
            case WOODCUTTING -> "\uDB00\uDC01\uE003\uDAFF\uDFFF";
            default -> "";
        };
    }

    public MaterialInfo getMaterialInfo() {
        return materialInfo;
    }
}

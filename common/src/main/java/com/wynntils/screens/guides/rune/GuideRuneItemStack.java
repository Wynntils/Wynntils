/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.rune;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.models.items.items.game.RuneItem;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class GuideRuneItemStack extends GuideItemStack {
    private static final CustomColor RESOURCE_COLOR = CustomColor.fromInt(0xf4aeae);
    private static final CustomColor RUNE_COLOR = CustomColor.fromInt(0xdf1b64);

    private List<Component> generatedTooltip;
    private final RuneType runeType;

    public GuideRuneItemStack(RuneType runeType) {
        super(getItemStack(runeType), new RuneItem(runeType), EnumUtils.toNiceString(runeType) + " Rune");

        this.runeType = runeType;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    private static ItemStack getItemStack(RuneType runeType) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        CustomModelData customModelData = new CustomModelData(
                List.of(Services.CustomModel.getFloat("rune_" + runeType.name().toLowerCase(Locale.ROOT))
                        .orElse(-1f)),
                List.of(),
                List.of(),
                List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        return itemStack;
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
                .append(Component.literal("\uE035")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(EnumUtils.toNiceString(runeType) + " Rune")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(runeType.getColor().asInt())));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerBoxFont.buildMessage("rune", RUNE_COLOR, CommonColors.BLACK, "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage("resource", RESOURCE_COLOR, CommonColors.BLACK, ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);

        MutableComponent usageLine1 = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("Use this item to enter "))
                .append(Component.literal("Raids").withStyle(ChatFormatting.WHITE));
        MutableComponent usageLine2 = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("or craft a "))
                .append(Component.literal("Corrupted Dungeon").withStyle(ChatFormatting.WHITE));
        Component usageLine3 = Component.literal("Key")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.WHITE));
        tooltipLines.add(usageLine1);
        tooltipLines.add(usageLine2);
        tooltipLines.add(usageLine3);

        tooltipLines.add(Component.empty());
        MutableComponent obtainLine1 = Component.literal("Obtained from ")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("World Events").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(","));
        MutableComponent obtainLine2 = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("Raids").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(", "))
                .append(Component.literal("Rune Guardians").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(","));
        MutableComponent obtainLine3 = Component.empty()
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("Shrines").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" and "))
                .append(Component.literal("Altars").withStyle(ChatFormatting.AQUA));
        tooltipLines.add(obtainLine1);
        tooltipLines.add(obtainLine2);
        tooltipLines.add(obtainLine3);

        int widestLine = tooltipLines.stream()
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);
        Component divider = Component.literal("\uE000")
                .withStyle(
                        Style.EMPTY.withFont(CommonFonts.TOOLTIP_DIVIDER_FONT).withColor(RESOURCE_COLOR.asInt()));
        int currentWidth = McUtils.mc().font.width(divider);
        int target = currentWidth + ((widestLine - currentWidth) / 2);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);
        Component centeredDivider = Component.empty()
                .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(divider);
        tooltipLines.add(3, centeredDivider);

        return tooltipLines;
    }

    public RuneType getRuneType() {
        return runeType;
    }
}

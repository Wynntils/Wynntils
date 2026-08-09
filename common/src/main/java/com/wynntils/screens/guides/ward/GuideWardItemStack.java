/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ward;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.models.items.items.game.WardItem;
import com.wynntils.models.rewards.type.WardType;
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

public class GuideWardItemStack extends GuideItemStack {
    private static final CustomColor RESOURCE_COLOR = CustomColor.fromInt(0xf4aeae);
    private static final CustomColor WARD_COLOR = CustomColor.fromInt(0xdf1b64);

    private List<Component> generatedTooltip;
    private final WardType wardType;

    public GuideWardItemStack(WardType wardType) {
        super(getItemStack(wardType), new WardItem(wardType), EnumUtils.toNiceString(wardType) + " Ward");

        this.wardType = wardType;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.addAll(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    private static ItemStack getItemStack(WardType wardType) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        CustomModelData customModelData = new CustomModelData(
                List.of(Services.CustomModel.getFloat("ward_" + wardType.name().toLowerCase(Locale.ROOT))
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
                .append(Component.literal("\uE036")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(EnumUtils.toNiceString(wardType) + " Ward")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(wardType.getColor().asInt())));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerBoxFont.buildMessage("ward", WARD_COLOR, CommonColors.BLACK, "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage("resource", RESOURCE_COLOR, CommonColors.BLACK, ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);

        MutableComponent usageLine1 = Component.literal("A powerful artifact used to")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY));
        MutableComponent usageLine2 = Component.literal("ascend items to new heights")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY));
        Component usageLine3 = Component.empty()
                .append(Component.literal("through the ")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)))
                .append(Component.literal("\uE00A")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.MERCHANT_FONT))
                        .withoutShadow())
                .append(Component.literal(" Item Upgrader")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE)))
                .append(Component.literal(".")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        tooltipLines.add(usageLine1);
        tooltipLines.add(usageLine2);
        tooltipLines.add(usageLine3);

        tooltipLines.add(Component.empty());
        MutableComponent obtainLine = Component.literal("Obtained from ")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal(EnumUtils.toNiceString(wardType.getItemObtainType()) + "s")
                        .withStyle(ChatFormatting.AQUA));
        tooltipLines.add(obtainLine);

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

    public WardType getWardType() {
        return wardType;
    }
}

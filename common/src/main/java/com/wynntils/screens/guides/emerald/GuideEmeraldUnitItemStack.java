/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class GuideEmeraldUnitItemStack extends GuideEmeraldItemStack {
    private final EmeraldUnits unit;

    public GuideEmeraldUnitItemStack(EmeraldUnits unit) {
        super(unit.getItemStack(), new EmeraldItem(() -> 1, unit), unit.name());
        this.unit = unit;
    }

    public EmeraldUnits getEmeraldUnit() {
        return unit;
    }

    @Override
    public List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();

        Component name =
                Component.empty().withStyle(ChatFormatting.GREEN).append(Component.literal(unit.getDisplayName()));
        itemLore.add(name);

        itemLore.add(getBanner());
        itemLore.add(Component.empty());

        Component worthLine = Component.translatable("screens.wynntils.wynntilsGuides.emeralds.emeralds.usage1")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(unit.getMultiplier() + EmeraldUnits.EMERALD.getSymbol())
                        .withStyle(ChatFormatting.GREEN));

        int widestLine = itemLore.stream()
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);
        int currentWidth = McUtils.mc().font.width(worthLine);
        int target = currentWidth + ((widestLine - currentWidth) / 2);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);

        Component centeredWorthLine = Component.empty()
                .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(worthLine);

        itemLore.add(centeredWorthLine);

        return itemLore;
    }

    @Override
    public int getTier() {
        return -1;
    }

    private Component getBanner() {
        return switch (unit) {
            case EMERALD_BLOCK -> buildBanner("eb");
            case LIQUID_EMERALD -> buildBanner("le");
            default -> buildBanner("e");
        };
    }

    private Component buildBanner(String text) {
        return Component.empty()
                .append(BannerBoxFont.buildMessage(
                        text,
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GREEN),
                        CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                        "\uDB00\uDC02"))
                .append(BannerBoxFont.buildMessage(
                        "currency",
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(ChatFormatting.WHITE),
                        ""));
    }
}

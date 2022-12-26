/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.EmeraldSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class EmeraldPouchItemStack extends WynnItemStack {
    private final int tier;
    private final boolean generated;

    private final List<Component> generatedTooltip;

    public EmeraldPouchItemStack(ItemStack stack) {
        super(stack);
        generated = false;

        Matcher matcher = WynnItemMatchers.emeraldPouchTierMatcher(stack.getHoverName());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("ItemStack name did not match emerald pouch tier matcher");
        }

        tier = MathUtils.integerFromRoman(matcher.group(1));
        generatedTooltip = List.of();
    }

    public EmeraldPouchItemStack(int tier) {
        super(new ItemStack(Items.DIAMOND_AXE));
        this.setDamageValue(97);

        generated = true;
        this.tier = tier;

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        generatedTooltip = generatePouchLore(tier);
    }

    private static List<Component> generatePouchLore(int tier) {
        int upTo = (tier - 1) / 3;
        int rows = 0;
        String totalString = "";

        switch (tier % 3) {
            case 0 -> {
                rows = 6;
                totalString = "54";
            }
            case 1 -> {
                rows = 1;
                totalString = "9";
            }
            case 2 -> {
                rows = 3;
                totalString = "27";
            }
        }

        if (tier >= 10) {
            rows = 6;
        }

        if (tier >= 7) {
            totalString = tier - 6 + "stx";
        } else if (tier >= 4) {
            totalString += EmeraldSymbols.L_STRING + EmeraldSymbols.E_STRING;
        } else {
            totalString += EmeraldSymbols.E_STRING + EmeraldSymbols.B_STRING;
        }

        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Emerald Pouches allows the wearer to easily ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("store ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("and ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("convert ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("picked emeralds without spending extra inventory slots.")
                        .withStyle(ChatFormatting.GRAY)));
        itemLore.add(Component.empty());
        itemLore.add(Component.literal(" - " + rows + " Rows ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("(" + totalString + " Total)"))
                .withStyle(ChatFormatting.DARK_GRAY));

        switch (upTo) {
            case 0 -> itemLore.add(Component.literal("No Auto-Conversions").withStyle(ChatFormatting.GRAY));
            case 1 -> itemLore.add(Component.literal("Converts up to")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" Emerald Blocks").withStyle(ChatFormatting.WHITE)));
            default -> itemLore.add(Component.literal("Converts up to")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" Liquid Emeralds").withStyle(ChatFormatting.WHITE)));
        }

        return itemLore;
    }

    @Override
    public Component getHoverName() {
        return generated
                ? Component.literal("Emerald Pouch ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("[Tier " + MathUtils.toRoman(tier) + "]")
                                .withStyle(ChatFormatting.DARK_GREEN))
                : super.getHoverName();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        if (!generated) {
            return super.getTooltipLines(player, flag);
        }

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.addAll(generatedTooltip);

        return tooltip;
    }

    public int getTier() {
        return tier;
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.PowderProfile;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GuidePowderItemStack extends GuideItemStack {
    private final int tier;
    private final Powder element;
    private final PowderProfile powderProfile;

    private final List<Component> generatedTooltip;

    public GuidePowderItemStack(PowderProfile profile) {
        super(getItemStack(profile));

        this.element = profile.element();
        this.tier = profile.tier();
        this.powderProfile = profile;
        this.generatedTooltip = generateLore();
    }

    @Override
    public Component getHoverName() {
        return Component.literal(element.getSymbol() + " " + element.getName() + " Powder " + MathUtils.toRoman(tier))
                .withStyle(element.getLightColor());
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.addAll(generatedTooltip);

        return tooltip;
    }

    private List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();

        String tierStringBuilder = element.getDarkColor()
                + "■".repeat(Math.max(0, tier))
                + ChatFormatting.DARK_GRAY
                + "■".repeat(Math.max(0, 6 - tier));

        String name = element.getName();
        Powder opposingElement = element.getOpposingElement();

        itemLore.add(Component.literal("Tier " + tier + " [")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(tierStringBuilder))
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY)));
        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Effect on Weapons:").withStyle(element.getDarkColor()));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+" + powderProfile.min()
                + "-" + powderProfile.max() + " " + element.getLightColor() + element.getSymbol() + " " + name + " "
                + ChatFormatting.GRAY + "Damage"));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+"
                + powderProfile.convertedFromNeutral() + "% " + ChatFormatting.GOLD + "✣ Neutral" + ChatFormatting.GRAY
                + " to " + element.getLightColor() + element.getSymbol() + " " + name));
        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Effect on Armour:").withStyle(element.getDarkColor()));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+"
                + powderProfile.addedDefence() + " " + element.getLightColor() + element.getSymbol() + " " + name + " "
                + ChatFormatting.GRAY + "Defence"));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "-"
                + powderProfile.removedDefence() + " " + opposingElement.getLightColor() + opposingElement.getSymbol()
                + " " + StringUtils.capitalizeFirst(opposingElement.name().toLowerCase(Locale.ROOT)) + " "
                + ChatFormatting.GRAY + "Defence"));
        itemLore.add(Component.empty());
        itemLore.add(Component.literal(
                        "Add this powder to your items by visiting a Powder Master or use it as an ingredient when crafting.")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (tier > 3) {
            itemLore.add(Component.empty());
            itemLore.add(Component.literal(
                            "Adding 2 powders of tier 4-6 at the powder master will unlock a special attack/effect.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        return itemLore;
    }

    private static ItemStack getItemStack(PowderProfile profile) {
        if (profile.tier() <= 3) {
            return new ItemStack(profile.element().getLowTierItem());
        } else {
            return new ItemStack(profile.element().getHighTierItem());
        }
    }

    public int getTier() {
        return tier;
    }

    public Powder getElement() {
        return element;
    }
}

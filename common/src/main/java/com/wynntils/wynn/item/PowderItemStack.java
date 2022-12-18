/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.item.generator.PowderGenerator;
import com.wynntils.wynn.item.generator.PowderProfile;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.Powder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class PowderItemStack extends WynnItemStack {
    private final int tier;
    private final Powder element;
    private final PowderProfile powderProfile;

    private final boolean generated;

    private final List<Component> generatedTooltip;

    public PowderItemStack(ItemStack stack) {
        super(stack);

        Matcher matcher = WynnItemMatchers.powderNameMatcher(this.getHoverName());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("ItemStack name did not match powder matcher");
        }

        element = Powder.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        tier = MathUtils.integerFromRoman(matcher.group(2));
        powderProfile = PowderGenerator.generatePowderProfile(element, tier);
        generated = false;
        generatedTooltip = List.of();
    }

    public PowderItemStack(PowderProfile profile) {
        super(getItemStack(profile));

        this.element = profile.element();
        this.tier = profile.tier();
        this.powderProfile = profile;
        this.generated = true;
        this.generatedTooltip = generateLore();
    }

    @Override
    public Component getHoverName() {
        return generated
                ? Component.literal(
                                element.getSymbol() + " " + element.getName() + " Powder " + MathUtils.toRoman(tier))
                        .withStyle(element.getLightColor())
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
        itemLore.add(TextComponent.EMPTY);
        itemLore.add(Component.literal("Effect on Weapons:").withStyle(element.getDarkColor()));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+" + powderProfile.min()
                + "-" + powderProfile.max() + " " + element.getLightColor() + element.getSymbol() + " " + name + " "
                + ChatFormatting.GRAY + "Damage"));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+"
                + powderProfile.convertedFromNeutral() + "% " + ChatFormatting.GOLD + "✣ Neutral" + ChatFormatting.GRAY
                + " to " + element.getLightColor() + element.getSymbol() + " " + name));
        itemLore.add(TextComponent.EMPTY);
        itemLore.add(Component.literal("Effect on Armour:").withStyle(element.getDarkColor()));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "+"
                + powderProfile.addedDefence() + " " + element.getLightColor() + element.getSymbol() + " " + name + " "
                + ChatFormatting.GRAY + "Defence"));
        itemLore.add(Component.literal(element.getDarkColor() + "— " + ChatFormatting.GRAY + "-"
                + powderProfile.removedDefence() + " " + opposingElement.getLightColor() + opposingElement.getSymbol()
                + " " + StringUtils.capitalizeFirst(opposingElement.name().toLowerCase(Locale.ROOT)) + " "
                + ChatFormatting.GRAY + "Defence"));
        itemLore.add(TextComponent.EMPTY);
        itemLore.add(Component.literal(
                        "Add this powder to your items by visiting a Powder Master or use it as an ingredient when crafting.")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (tier > 3) {
            itemLore.add(TextComponent.EMPTY);
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

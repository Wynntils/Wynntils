/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GuidePowderItemStack extends GuideItemStack {
    private final int tier;
    private final Powder element;
    private final PowderTierInfo powderTierInfo;

    private final List<Component> generatedTooltip;

    public GuidePowderItemStack(PowderTierInfo powderTierInfo) {
        super(
                getItemStack(powderTierInfo),
                new PowderItem(powderTierInfo),
                powderTierInfo.element().getName() + " Powder");

        this.element = powderTierInfo.element();
        this.tier = powderTierInfo.tier();
        this.powderTierInfo = powderTierInfo;
        this.generatedTooltip = generateLore();
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>(generatedTooltip);

        return tooltip;
    }

    private List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();

        String elementName = element.getName();
        Powder opposingElement = Models.Element.getOpposingElement(element);

        Component name = Component.empty()
                .withStyle(Style.EMPTY.withColor(element.getLightColor()))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT)))
                .append(Component.literal(" " + elementName + " Powder " + MathUtils.toRoman(tier)));
        itemLore.add(name);

        Component tierBar = Component.empty()
                .append(Component.literal("■".repeat(Math.max(0, tier))).withColor(element.getDarkColor()))
                .append(Component.literal("■".repeat(Math.max(0, 6 - tier))).withStyle(ChatFormatting.DARK_GRAY));

        Component tierLore = Component.empty()
                .append(Component.literal("Tier " + tier + " [").withStyle(ChatFormatting.GRAY))
                .append(tierBar)
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
        itemLore.add(tierLore);

        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Effect on Weapons:").withColor(element.getDarkColor()));

        Component weaponDamage = Component.empty()
                .append(Component.literal("- ").withColor(element.getDarkColor()))
                .append(Component.literal("+" + powderTierInfo.min() + "-" + powderTierInfo.max() + " ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(" " + elementName + " ").withColor(element.getLightColor()))
                .append(Component.literal("Damage").withStyle(ChatFormatting.GRAY));
        itemLore.add(weaponDamage);

        Component neutralConversion = Component.empty()
                .append(Component.literal("- ").withColor(element.getDarkColor()))
                .append(Component.literal("+" + powderTierInfo.convertedFromNeutral() + "% ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("✣ Neutral").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(" " + elementName).withColor(element.getLightColor()));
        itemLore.add(neutralConversion);

        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Effect on Armour:").withColor(element.getDarkColor()));

        Component health = Component.empty()
                .append(Component.literal("- ").withColor(element.getDarkColor()))
                .append(Component.literal("+" + powderTierInfo.health() + " Health")
                        .withStyle(ChatFormatting.GRAY));
        itemLore.add(health);

        Component addedDefence = Component.empty()
                .append(Component.literal("- ").withColor(element.getDarkColor()))
                .append(Component.literal("+" + powderTierInfo.addedDefence() + " ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(" " + elementName + " ").withColor(element.getLightColor()))
                .append(Component.literal("Defence").withStyle(ChatFormatting.GRAY));
        itemLore.add(addedDefence);

        String opposingElementName =
                StringUtils.capitalizeFirst(opposingElement.name().toLowerCase(Locale.ROOT));

        Component removedDefence = Component.empty()
                .append(Component.literal("- ").withColor(element.getDarkColor()))
                .append(Component.literal("-" + powderTierInfo.removedDefence() + " ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(opposingElement.getSymbol()))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.COMMON_FONT)
                                .withColor(opposingElement.getLightColor())))
                .append(Component.literal(" " + opposingElementName + " ").withColor(opposingElement.getLightColor()))
                .append(Component.literal("Defence").withStyle(ChatFormatting.GRAY));
        itemLore.add(removedDefence);

        Component ingredientEffectiveness = Component.empty()
                .append(Component.literal("Ingredient Effectiveness: ").withColor(element.getDarkColor()))
                .append(Component.literal("50%").withStyle(ChatFormatting.GRAY));
        itemLore.add(ingredientEffectiveness);

        itemLore.add(Component.empty());
        itemLore.add(Component.literal(
                        "Hold this and right-click on a piece of equipment to socket it or use it as an ingredient when crafting. Powders are refunded when removed.")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (tier > 3) {
            itemLore.add(Component.empty());
            itemLore.add(Component.literal("Adding 2 powders of Tier 4 or higher will unlock a special attack/effect")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        return ComponentUtils.wrapTooltips(itemLore, 200);
    }

    private static ItemStack getItemStack(PowderTierInfo profile) {
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

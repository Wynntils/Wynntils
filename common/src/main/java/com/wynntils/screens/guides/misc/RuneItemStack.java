/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.misc;

import com.wynntils.core.components.Services;
import com.wynntils.models.items.items.game.RuneItem;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.EnumUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class RuneItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;
    private final RuneType runeType;

    public RuneItemStack(RuneType runeType) {
        super(getItemStack(runeType), new RuneItem(runeType), EnumUtils.toNiceString(runeType) + " Rune");

        this.runeType = runeType;
    }

    @Override
    public Component getHoverName() {
        return Component.literal(EnumUtils.toNiceString(runeType) + " Rune")
                .withColor(getRuneType().getColor().asInt());
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(getHoverName());
            tooltip.add(Component.empty());
            tooltip.addAll(generateLore());

            appendFavoriteInfo(tooltip);

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
        MutableComponent usageComponent = Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.usage1")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.usage2")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.usage3")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.usage4")
                        .withStyle(ChatFormatting.WHITE));
        MutableComponent obtainComponent = Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.obtain1")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.runes.obtain2")
                        .withStyle(ChatFormatting.AQUA));

        return List.of(usageComponent, Component.empty(), obtainComponent);
    }

    public RuneType getRuneType() {
        return runeType;
    }
}

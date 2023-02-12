/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.ingredientinfo.IngredientInfo;
import com.wynntils.models.ingredients.profile.IngredientItemModifiers;
import com.wynntils.models.ingredients.profile.IngredientModifiers;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public final class GuideIngredientItemStack extends GuideItemStack {
    private final IngredientInfo ingredientInfo;
    private final List<MutableComponent> guideTooltip;

    public GuideIngredientItemStack(IngredientInfo ingredientInfo) {
        super(ingredientInfo.material().itemStack(), new IngredientItem(ingredientInfo), ingredientInfo.name());

        this.ingredientInfo = ingredientInfo;
        this.guideTooltip = generateGuideTooltip();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        tooltip.addAll(guideTooltip);

        tooltip.add(Component.empty());
        if (Models.Favorites.isFavorite(ingredientInfo.name())) {
            tooltip.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                    .withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.open")
                .withStyle(ChatFormatting.RED));

        return tooltip;
    }

    @Override
    public Component getHoverName() {
        return Component.literal(ingredientInfo.name())
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" " + getTierString(ingredientInfo.tier())));
    }

    private String getTierString(int tier) {
        return switch (tier) {
            case 0 -> ChatFormatting.GRAY + "[" + ChatFormatting.DARK_GRAY + "✫✫✫" + ChatFormatting.GRAY + "]";
            case 1 -> ChatFormatting.GOLD + "[" + ChatFormatting.YELLOW + "✫" + ChatFormatting.DARK_GRAY + "✫✫"
                    + ChatFormatting.GOLD + "]";
            case 2 -> ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "✫✫" + ChatFormatting.DARK_GRAY
                    + "✫" + ChatFormatting.DARK_PURPLE + "]";
            case 3 -> ChatFormatting.DARK_AQUA + "[" + ChatFormatting.AQUA + "✫✫✫" + ChatFormatting.DARK_AQUA + "]";
            default -> {
                WynntilsMod.warn("Invalid ingredient tier for: " + this.ingredientInfo.name() + ": " + tier);
                yield "";
            }
        };
    }

    private List<MutableComponent> generateGuideTooltip() {
        List<MutableComponent> itemLore = new ArrayList<>();

        itemLore.add(Component.literal("Crafting Ingredient").withStyle(ChatFormatting.DARK_GRAY));
        itemLore.add(Component.empty());

        List<Pair<StatType, RangedValue>> stats = ingredientInfo.variableStats();

        for (Pair<StatType, RangedValue> valuedStat : stats) {
            if (valuedStat.value().isFixed()) {
                if (valuedStat.value().low() >= 0) {
                    itemLore.add(Component.literal("+" + valuedStat.value().low()
                                    + valuedStat.key().getUnit().getDisplayName())
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" " + valuedStat.key().getDisplayName())
                                    .withStyle(ChatFormatting.GRAY)));
                } else {
                    itemLore.add(Component.literal(valuedStat.value().low()
                                    + valuedStat.key().getUnit().getDisplayName())
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(" " + valuedStat.key().getDisplayName())
                                    .withStyle(ChatFormatting.GRAY)));
                }
            } else {
                if (valuedStat.value().low() >= 0) {
                    itemLore.add(Component.literal("+" + valuedStat.value().low())
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GREEN))
                            .append(Component.literal(valuedStat.value().high()
                                            + valuedStat.key().getUnit().getDisplayName())
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(" " + valuedStat.key().getDisplayName())
                                    .withStyle(ChatFormatting.GRAY)));
                } else {
                    itemLore.add(Component.literal(
                                    String.valueOf(valuedStat.value().low()))
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_RED))
                            .append(Component.literal(valuedStat.value().high()
                                            + valuedStat.key().getUnit().getDisplayName())
                                    .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" " + valuedStat.key().getDisplayName())
                                    .withStyle(ChatFormatting.GRAY)));
                }
            }
        }

        if (!stats.isEmpty()) {
            itemLore.add(Component.empty());
        }

        IngredientModifiers ingredientModifiers = ingredientInfo.getIngredientModifiers();
        itemLore.addAll(ingredientModifiers.getModifierLoreLines());

        if (ingredientModifiers.anyExists()) {
            itemLore.add(Component.empty());
        }

        IngredientItemModifiers itemModifiers = ingredientInfo.getItemModifiers();
        itemLore.addAll(itemModifiers.getItemModifierLoreLines());

        if (itemModifiers.anyExists()) {
            itemLore.add(Component.empty());
        }

        itemLore.add(
                Component.literal("Crafting Lv. Min: " + ingredientInfo.level()).withStyle(ChatFormatting.GRAY));

        for (ProfessionType profession : ingredientInfo.professions()) {
            itemLore.add(Component.literal("  " + profession.getProfessionIconChar() + " ")
                    .append(Component.literal(profession.getDisplayName()).withStyle(ChatFormatting.GRAY)));
        }

        return itemLore;
    }

    public IngredientInfo getIngredientInfo() {
        return ingredientInfo;
    }
}

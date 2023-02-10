/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.ingredients.profile.IdentificationProfile;
import com.wynntils.models.ingredients.profile.IngredientIdentificationContainer;
import com.wynntils.models.ingredients.profile.IngredientItemModifiers;
import com.wynntils.models.ingredients.profile.IngredientModifiers;
import com.wynntils.models.ingredients.profile.IngredientProfile;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public final class GuideIngredientItemStack extends GuideItemStack {
    private final List<MutableComponent> guideTooltip;

    private final IngredientProfile ingredientProfile;

    public GuideIngredientItemStack(IngredientProfile ingredientProfile) {
        super(
                ingredientProfile.asItemStack(),
                new IngredientItem(ingredientProfile),
                ingredientProfile.getDisplayName());

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        this.ingredientProfile = ingredientProfile;

        guideTooltip = generateGuideTooltip();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        tooltip.addAll(guideTooltip);

        tooltip.add(Component.empty());
        if (Models.Favorites.isFavorite(ingredientProfile.getDisplayName())) {
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
        return Component.literal(ingredientProfile.getDisplayName())
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" " + ingredientProfile.getTier().getTierString()));
    }

    private List<MutableComponent> generateGuideTooltip() {
        List<MutableComponent> itemLore = new ArrayList<>();

        itemLore.add(Component.literal("Crafting Ingredient").withStyle(ChatFormatting.DARK_GRAY));
        itemLore.add(Component.empty());

        Map<String, IngredientIdentificationContainer> statuses = ingredientProfile.getStatuses();

        for (String status : statuses.keySet()) {
            IngredientIdentificationContainer identificationContainer = statuses.get(status);
            if (identificationContainer.hasConstantValue()) {
                if (identificationContainer.getMin() >= 0) {
                    itemLore.add(Component.literal("+" + identificationContainer.getMin()
                                    + identificationContainer.getType().getInGame(status))
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" " + IdentificationProfile.getAsLongName(status))
                                    .withStyle(ChatFormatting.GRAY)));
                } else {
                    itemLore.add(Component.literal(identificationContainer.getMin()
                                    + identificationContainer.getType().getInGame(status))
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(" " + IdentificationProfile.getAsLongName(status))
                                    .withStyle(ChatFormatting.GRAY)));
                }
            } else {
                if (identificationContainer.getMin() >= 0) {
                    itemLore.add(Component.literal("+" + identificationContainer.getMin())
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GREEN))
                            .append(Component.literal(identificationContainer.getMax()
                                            + identificationContainer.getType().getInGame(status))
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(" " + IdentificationProfile.getAsLongName(status))
                                    .withStyle(ChatFormatting.GRAY)));
                } else {
                    itemLore.add(Component.literal(String.valueOf(identificationContainer.getMin()))
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_RED))
                            .append(Component.literal(identificationContainer.getMax()
                                            + identificationContainer.getType().getInGame(status))
                                    .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" " + IdentificationProfile.getAsLongName(status))
                                    .withStyle(ChatFormatting.GRAY)));
                }
            }
        }

        if (!statuses.isEmpty()) {
            itemLore.add(Component.empty());
        }

        IngredientModifiers ingredientModifiers = ingredientProfile.getIngredientModifiers();
        itemLore.addAll(ingredientModifiers.getModifierLoreLines());

        if (ingredientModifiers.anyExists()) {
            itemLore.add(Component.empty());
        }

        IngredientItemModifiers itemModifiers = ingredientProfile.getItemModifiers();
        itemLore.addAll(itemModifiers.getItemModifierLoreLines());

        if (itemModifiers.anyExists()) {
            itemLore.add(Component.empty());
        }

        if (ingredientProfile.isUntradeable())
            itemLore.add(Component.literal("Untradable Item").withStyle(ChatFormatting.RED));

        itemLore.add(Component.literal("Crafting Lv. Min: " + ingredientProfile.getLevel())
                .withStyle(ChatFormatting.GRAY));

        for (ProfessionType profession : ingredientProfile.getProfessions()) {
            itemLore.add(Component.literal("  " + profession.getProfessionIconChar() + " ")
                    .append(Component.literal(profession.getDisplayName()).withStyle(ChatFormatting.GRAY)));
        }

        return itemLore;
    }

    public IngredientProfile getIngredientProfile() {
        return ingredientProfile;
    }
}

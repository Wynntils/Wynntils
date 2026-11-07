/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.IngredientTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideIngredientItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;
    private final IngredientInfo ingredientInfo;

    private boolean statsPage = true;

    public GuideIngredientItemStack(IngredientInfo ingredientInfo) {
        super(ingredientInfo.material().itemStack(), new IngredientItem(ingredientInfo), ingredientInfo.name());

        this.ingredientInfo = ingredientInfo;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            generatedTooltip = IngredientTooltipBuilder.buildNewItem(
                            ingredientInfo, ItemSourceType.GUIDE, "", statsPage ? 0 : 1)
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    public void changePage() {
        statsPage = !statsPage;
        generatedTooltip = null;
    }

    public IngredientInfo getIngredientInfo() {
        return ingredientInfo;
    }
}

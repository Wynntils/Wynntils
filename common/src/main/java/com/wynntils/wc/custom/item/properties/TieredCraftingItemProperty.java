/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.type.HighlightProperty;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;

public abstract class TieredCraftingItemProperty extends ItemProperty implements HighlightProperty {
    protected final IngredientTier tier;

    public TieredCraftingItemProperty(WynnItemStack item) {
        super(item);

        String name = WynnUtils.normalizeBadString(item.getHoverName().getString());

        tier = calculateTier(name);
    }

    protected IngredientTier calculateTier(String name) {
        if (name.endsWith(getPrimaryColor(IngredientTier.ONE) + " [" + getSecondaryColor(IngredientTier.ONE) + "✫"
                + ChatFormatting.DARK_GRAY + "✫✫" + getPrimaryColor(IngredientTier.ONE) + "]")) {
            return IngredientTier.ONE;
        } else if (name.endsWith(getPrimaryColor(IngredientTier.TWO) + " [" + getSecondaryColor(IngredientTier.TWO)
                + "✫✫" + ChatFormatting.DARK_GRAY + "✫" + getPrimaryColor(IngredientTier.TWO) + "]")) {
            return IngredientTier.TWO;
        } else if (name.endsWith(getPrimaryColor(IngredientTier.THREE) + " [" + getSecondaryColor(IngredientTier.THREE)
                + "✫✫✫" + getPrimaryColor(IngredientTier.THREE) + "]")) {
            return IngredientTier.THREE;
        } else {
            return IngredientTier.ZERO;
        }
    }

    @Override
    public boolean isInventoryHighlight() {
        return true;
    }

    @Override
    public boolean isHotbarHighlight() {
        return false;
    }

    protected abstract ChatFormatting getPrimaryColor(IngredientTier tier);

    protected abstract ChatFormatting getSecondaryColor(IngredientTier tier);

    public enum IngredientTier {
        ZERO,
        ONE,
        TWO,
        THREE
    }
}

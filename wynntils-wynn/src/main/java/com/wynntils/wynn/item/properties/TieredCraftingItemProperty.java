/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.HighlightProperty;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.ChatFormatting;

public abstract class TieredCraftingItemProperty extends ItemProperty implements HighlightProperty {
    protected final IngredientTier tier;

    public TieredCraftingItemProperty(WynnItemStack item) {
        super(item);

        String name = WynnUtils.normalizeBadString(item.getHoverName().getString());

        tier = calculateTier(name);
    }

    protected IngredientTier calculateTier(String name) {
        if (name.endsWith(
                getPrimaryParsingColor(IngredientTier.ONE) + " [" + getSecondaryParsingColor(IngredientTier.ONE) + "✫"
                        + ChatFormatting.DARK_GRAY + "✫✫" + getPrimaryParsingColor(IngredientTier.ONE) + "]")) {
            return IngredientTier.ONE;
        } else if (name.endsWith(
                getPrimaryParsingColor(IngredientTier.TWO) + " [" + getSecondaryParsingColor(IngredientTier.TWO) + "✫✫"
                        + ChatFormatting.DARK_GRAY + "✫" + getPrimaryParsingColor(IngredientTier.TWO) + "]")) {
            return IngredientTier.TWO;
        } else if (name.endsWith(
                getPrimaryParsingColor(IngredientTier.THREE) + " [" + getSecondaryParsingColor(IngredientTier.THREE)
                        + "✫✫✫" + getPrimaryParsingColor(IngredientTier.THREE) + "]")) {
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

    protected abstract ChatFormatting getPrimaryParsingColor(IngredientTier tier);

    protected abstract ChatFormatting getSecondaryParsingColor(IngredientTier tier);

    public enum IngredientTier {
        ZERO,
        ONE,
        TWO,
        THREE
    }
}

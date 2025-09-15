/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.inventory;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class IngredientPouchModel extends Model {
    private static final int MAX_INGREDIENT_POUCH_SLOTS = 27;

    public IngredientPouchModel() {
        super(List.of());
    }

    public CappedValue getIngredientPouchSlots() {
        return new CappedValue(getUsedIngredientPouchSlots(), MAX_INGREDIENT_POUCH_SLOTS);
    }

    public int getIngredientAmountInPouch(String name) {
        ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);
        Optional<IngredientPouchItem> pouchItemOpt = Models.Item.asWynnItem(itemStack, IngredientPouchItem.class);

        // This should never happen
        if (pouchItemOpt.isEmpty()) {
            WynntilsMod.warn("Could not find Ingredient Pouch");
            return -1;
        }

        IngredientPouchItem pouchItem = pouchItemOpt.get();
        return pouchItem.getIngredients().stream()
                .filter(ingredientInfoIntegerPair -> {
                    IngredientInfo info = ingredientInfoIntegerPair.a();
                    return info.name().startsWith(name);
                })
                .mapToInt(Pair::b)
                .sum();
    }

    private int getUsedIngredientPouchSlots() {
        ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);
        Optional<IngredientPouchItem> pouchItemOpt = Models.Item.asWynnItem(itemStack, IngredientPouchItem.class);

        // This should never happen
        if (pouchItemOpt.isEmpty()) return -1;

        return pouchItemOpt.get().getIngredients().size();
    }
}

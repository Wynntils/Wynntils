/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.utils.wynn.InventoryUtils;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class IngredientPouchHotkeyFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind ingredientPouchKeyBind =
            new KeyBind("Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, true, this::onOpenIngredientPouchKeyPress);

    private void onOpenIngredientPouchKeyPress() {
        if (!Models.WorldState.onWorld()) return;

        InventoryUtils.sendInventorySlotMouseClick(
                InventoryUtils.INGREDIENT_POUCH_SLOT_NUM, InventoryUtils.MouseClickType.LEFT_CLICK);
    }
}

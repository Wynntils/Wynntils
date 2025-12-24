/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.utils.wynn.InventoryUtils;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class IngredientPouchHotkeyFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind ingredientPouchKeyBind =
            new KeyBind("Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, true, this::onOpenIngredientPouchKeyPress);

    public IngredientPouchHotkeyFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    private void onOpenIngredientPouchKeyPress() {
        if (!Models.WorldState.onWorld()) return;

        InventoryUtils.sendInventorySlotMouseClick(
                InventoryUtils.INGREDIENT_POUCH_SLOT_NUM, InventoryUtils.MouseClickType.LEFT_CLICK);
    }
}

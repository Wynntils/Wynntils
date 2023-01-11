/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.wynn.utils.InventoryUtils;
import com.wynntils.wynn.utils.WynnUtils;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE)
public class IngredientPouchHotkeyFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind ingredientPouchKeyBind =
            new KeyBind("Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, true, this::onOpenIngredientPouchKeyPress);

    private void onOpenIngredientPouchKeyPress() {
        if (!WynnUtils.onWorld()) return;

        InventoryUtils.sendInventorySlotMouseClick(
                InventoryUtils.INGREDIENT_POUCH_SLOT_NUM, InventoryUtils.MouseClickType.LEFT_CLICK);
    }
}

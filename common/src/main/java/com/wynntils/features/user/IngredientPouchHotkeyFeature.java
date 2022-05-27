/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import static com.wynntils.mc.utils.InventoryUtils.MouseClickType.LEFT_CLICK;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.GameplayImpact;
import com.wynntils.core.features.properties.FeatureInfo.PerformanceImpact;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.KeyBinds;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.mc.utils.InventoryUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import org.lwjgl.glfw.GLFW;

@KeyBinds
@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class IngredientPouchHotkeyFeature extends UserFeature {
    public static IngredientPouchHotkeyFeature INSTANCE;

    private final KeyHolder ingredientPouchKeybind = new KeyHolder(
            "Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, this::onOpenIngredientPouchKeyPress);

    private void onOpenIngredientPouchKeyPress() {
        if (!WynnUtils.onWorld()) return;

        InventoryUtils.sendInventorySlotMouseClick(
                InventoryUtils.INGREDIENT_POUCH_SLOT_NUM,
                McUtils.inventory().getItem(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM),
                LEFT_CLICK);
    }
}

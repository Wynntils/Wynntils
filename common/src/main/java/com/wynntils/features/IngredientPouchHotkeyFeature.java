/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.wc.utils.WynnUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class IngredientPouchHotkeyFeature extends FeatureBase {

    private static final int INGREDIENT_POUCH_SLOT_NUM = 13;

    private final KeyHolder ingredientPouchKeybind = new KeyHolder(
            "Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, this::onOpenIngredientPouchKeyPress);

    public IngredientPouchHotkeyFeature() {
        setupKeyHolder(ingredientPouchKeybind);
    }

    private void onOpenIngredientPouchKeyPress() {
        if (!WynnUtils.onWorld()) return;

        Player player = McUtils.player();
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();

        changedSlots.putIfAbsent(
                INGREDIENT_POUCH_SLOT_NUM, player.getInventory().getItem(INGREDIENT_POUCH_SLOT_NUM));
        McUtils.player()
                .connection
                .send(new ServerboundContainerClickPacket(
                        player.inventoryMenu.containerId,
                        player.inventoryMenu.getStateId(),
                        INGREDIENT_POUCH_SLOT_NUM,
                        0,
                        ClickType.PICKUP,
                        ItemStack.EMPTY,
                        changedSlots));
    }
}

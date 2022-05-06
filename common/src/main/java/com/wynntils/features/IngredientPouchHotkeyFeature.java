/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.wc.utils.WynnUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class IngredientPouchHotkeyFeature extends Feature {

    private final int ingredientPouchSlotNum = 13;

    private final KeyHolder ingredientPouchKeybind =
            new KeyHolder("Open Ingredient Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, () -> {
                if (!WynnUtils.onWorld()) return;

                Player player = McUtils.player();
                Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();

                changedSlots.putIfAbsent(
                        ingredientPouchSlotNum, player.getInventory().getItem(13));
                McUtils.player()
                        .connection
                        .send(new ServerboundContainerClickPacket(
                                player.inventoryMenu.containerId,
                                player.inventoryMenu.getStateId(),
                                ingredientPouchSlotNum,
                                0,
                                ClickType.PICKUP,
                                ItemStack.EMPTY,
                                changedSlots));
            });

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.ingredientPouchKeybind.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        KeyManager.registerKeybind(ingredientPouchKeybind);
        return true;
    }

    @Override
    protected void onDisable() {
        KeyManager.unregisterKeybind(ingredientPouchKeybind);
    }
}

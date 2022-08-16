/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class LogItemInfoFeature extends DebugFeature {
    @RegisterKeyBind
    private final KeyHolder logItemInfoKeybind =
            new KeyHolder("Log Item Info", GLFW.GLFW_KEY_INSERT, "Wynntils", true, this::onLogItemInfoPress);

    @SubscribeEvent
    public void onKeyPress(InventoryKeyPressEvent e) {
        if (logItemInfoKeybind.getKeybind().matches(e.getKeyCode(), e.getScanCode())) {
            Slot hoveredSlot = e.getHoveredSlot();
            if (hoveredSlot == null) return;
            logItem(hoveredSlot.getItem());
        }
    }

    private void onLogItemInfoPress() {
        logItem(McUtils.mc().player.getItemBySlot(EquipmentSlot.MAINHAND));
    }

    private void logItem(ItemStack itemStack) {
        String sb = "[Logging Item]\nName: "
                + itemStack.getHoverName().getString() + "\nLore:\n"
                + String.join("\n", ItemUtils.getLore(itemStack)) + "\nItem Type: "
                + itemStack.getItem() + "\nDamage Value: "
                + itemStack.getDamageValue() + "\nNBT: "
                + itemStack.getTag() + "\nGlint: "
                + itemStack.hasFoil();

        WynntilsMod.info(sb);
    }
}

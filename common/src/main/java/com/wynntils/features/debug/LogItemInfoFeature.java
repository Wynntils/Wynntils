/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@StartDisabled
@ConfigCategory(Category.DEBUG)
public class LogItemInfoFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind logItemInfoKeyBind = new KeyBind(
            "Log Item Info", GLFW.GLFW_KEY_INSERT, true, this::onLogItemInfoPress, this::onLogItemInfoInventoryPress);

    private void onLogItemInfoPress() {
        logItem(McUtils.player().getItemBySlot(EquipmentSlot.MAINHAND));
    }

    private void onLogItemInfoInventoryPress(Slot hoveredSlot) {
        if (hoveredSlot == null) return;
        logItem(hoveredSlot.getItem());
    }

    private void logItem(ItemStack itemStack) {
        String sb = "[Logging Item]\nName: "
                + itemStack.getHoverName().getString() + "\nLore:\n"
                + String.join("\n", LoreUtils.getLore(itemStack)) + "\nItem Type: "
                + itemStack.getItem() + "\nDamage Value: "
                + itemStack.getDamageValue() + "\nNBT: "
                + itemStack.getTag() + "\nGlint: "
                + itemStack.hasFoil();

        WynntilsMod.info(sb);
    }
}

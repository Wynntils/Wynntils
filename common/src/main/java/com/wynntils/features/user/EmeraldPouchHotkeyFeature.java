/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.handleditems.items.game.EmeraldPouchItem;
import com.wynntils.wynn.utils.InventoryUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE)
public class EmeraldPouchHotkeyFeature extends UserFeature {
    private static final int NO_POUCHES = -1;
    private static final int MULTIPLE_POUCHES = -2;

    @RegisterKeyBind
    private final KeyBind emeraldPouchKeyBind =
            new KeyBind("Open Emerald Pouch", GLFW.GLFW_KEY_UNKNOWN, true, this::onOpenPouchKeyPress);

    private void onOpenPouchKeyPress() {
        if (!WynnUtils.onWorld()) return;

        int slotNumber = getEmeraldPouchSlot();

        if (slotNumber == NO_POUCHES) {
            // We found no emerald pouches at all
            Managers.Notification.queueMessage(new TextRenderTask(
                    ChatFormatting.RED + I18n.get("feature.wynntils.emeraldPouchHotkey.noPouch"),
                    TextRenderSetting.DEFAULT.withCustomColor(CommonColors.RED)));
            return;
        }
        if (slotNumber == MULTIPLE_POUCHES) {
            // We found more than one filled pouch, cannot choose between them
            Managers.Notification.queueMessage(new TextRenderTask(
                    ChatFormatting.RED + I18n.get("feature.wynntils.emeraldPouchHotkey.multipleFilled"),
                    TextRenderSetting.DEFAULT.withCustomColor(CommonColors.RED)));
            return;
        }

        // We found exactly one usable emerald pouch

        if (slotNumber < 9) {
            // Raw slot numbers, remap if in hotbar
            slotNumber += 36;
        }

        InventoryUtils.sendInventorySlotMouseClick(slotNumber, InventoryUtils.MouseClickType.RIGHT_CLICK);
    }

    private int getEmeraldPouchSlot() {
        int bestEmptyTier = -1;
        int bestEmptySlot = -1;
        int foundNonEmptySlot = -1;

        // Look through the entire inventory after any pouches
        Container inventory = McUtils.inventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            Optional<EmeraldPouchItem> optPouchItem = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
            if (optPouchItem.isEmpty()) continue;

            EmeraldPouchItem pouchItem = optPouchItem.get();
            if (pouchItem.getValue() == 0) {
                // It's empty
                int tier = pouchItem.getTier();
                if (tier > bestEmptyTier) {
                    bestEmptySlot = slot;
                    bestEmptyTier = tier;
                }
            } else if (foundNonEmptySlot != -1) {
                // We've already found one non-empty pouch; can't choose between them
                return MULTIPLE_POUCHES;
            } else {
                foundNonEmptySlot = slot;
            }
        }

        if (foundNonEmptySlot == -1 && bestEmptySlot == -1) return NO_POUCHES;

        if (foundNonEmptySlot != -1) {
            // We found just a single, non-empty pouch, so use that
            return foundNonEmptySlot;
        } else {
            // Return the empty pouch with the largest capacity.
            return bestEmptySlot;
        }
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.ItemCountOverlayEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ExtendedItemCountFeature extends UserFeature {
    @Config
    public boolean inventoryTextOverlayEnabled = true;

    @Config
    public boolean hotbarTextOverlayEnabled = true;

    private boolean isInventory;

    @SubscribeEvent
    public void onRenderSlotPre(SlotRenderEvent.Pre e) {
        isInventory = true;
    }

    @SubscribeEvent
    public void onRenderHotbarSlotPre(HotbarSlotRenderEvent.Pre e) {
        isInventory = false;
    }

    @SubscribeEvent
    public void onItemCountOverlay(ItemCountOverlayEvent event) {
        if (isInventory && !inventoryTextOverlayEnabled) return;
        if (!isInventory && !hotbarTextOverlayEnabled) return;

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;

        WynnItem wynnItem = wynnItemOpt.get();

        int count;
        CustomColor countColor;
        if (wynnItem instanceof LeveledItemProperty leveledItem
                && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)
                && isInventory) {
            event.setCountString(String.valueOf(leveledItem.getLevel()));
        } else if (wynnItem instanceof CountedItemProperty countedItem && countedItem.hasCount()) {
            event.setCountString(String.valueOf(countedItem.getCount()));
            event.setCountColor(countedItem.getCountColor().asInt());
        }
    }
}

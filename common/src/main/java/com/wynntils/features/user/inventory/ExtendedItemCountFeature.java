/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ExtendedItemCountFeature extends UserFeature {
    @Config
    public boolean inventoryTextOverlayEnabled = true;

    @Config
    public boolean hotbarTextOverlayEnabled = true;

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post e) {
        if (!inventoryTextOverlayEnabled) return;

        drawTextOverlay(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Post e) {
        if (!hotbarTextOverlayEnabled) return;

        drawTextOverlay(e.getItemStack(), e.getX(), e.getY());
    }

    private void drawTextOverlay(ItemStack itemStack, int slotX, int slotY) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof CountedItemProperty countedItem)) return;

        if (!countedItem.hasCount()) return;
        int count = countedItem.getCount();
        // This is a bit ugly; would rather we hid the drawing but that was tricky to do
        // with mixins...
        itemStack.setCount(1);

        TextRenderSetting style = TextRenderSetting.DEFAULT
                .withCustomColor(countedItem.getCountColor())
                .withHorizontalAlignment(HorizontalAlignment.Right);

        TextRenderTask task = new TextRenderTask(Integer.toString(count), style);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, 300); // items are drawn at z300, so text has to be as well
        FontRenderer.getInstance().renderText(poseStack, slotX + 17, slotY + 9, task);
    }
}

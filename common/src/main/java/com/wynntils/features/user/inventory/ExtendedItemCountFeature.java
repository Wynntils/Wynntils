/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemCache;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ExtendedItemCountFeature extends UserFeature {
    @Config
    public boolean inventoryTextOverlayEnabled = true;

    @Config
    public boolean hotbarTextOverlayEnabled = true;

    private TextRenderTask countRenderTask;

    // First we must check if we should draw our own count, and thus request vanilla to normal count
    @SubscribeEvent
    public void onRenderSlotPre(SlotRenderEvent.Pre e) {
        if (!inventoryTextOverlayEnabled) return;

        countRenderTask = getCountRenderTask(e.getSlot().getItem());
    }

    @SubscribeEvent
    public void onRenderHotbarSlotPre(HotbarSlotRenderEvent.Pre e) {
        if (!hotbarTextOverlayEnabled) return;

        countRenderTask = getCountRenderTask(e.getStack());
    }

    // Then we can actually draw the text, which needs to be drawn on top of everything,
    // so make sure we get the event after e.g. gear box icons
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderSlotPost(SlotRenderEvent.Post e) {
        if (countRenderTask == null) return;

        drawTextOverlay(countRenderTask, e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
        countRenderTask = null;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHotbarSlotPost(HotbarSlotRenderEvent.Post e) {
        if (countRenderTask == null) return;

        drawTextOverlay(countRenderTask, e.getStack(), e.getX(), e.getY());
        countRenderTask = null;
    }

    private TextRenderTask getCountRenderTask(ItemStack item) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(item);
        if (wynnItemOpt.isEmpty()) return null;

        WynnItem wynnItem = wynnItemOpt.get();

        int count;
        CustomColor countColor;
        if (wynnItem instanceof LeveledItemProperty leveledItem
                && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            count = leveledItem.getLevel();
            countColor = CommonColors.WHITE;
        } else if (wynnItem instanceof CountedItemProperty countedItem && countedItem.hasCount()) {
            count = countedItem.getCount();
            countColor = countedItem.getCountColor();
        } else {
            return null;
        }

        // Request mixin to hide vanilla count
        wynnItem.getCache().store(WynnItemCache.HIDE_COUNT_KEY, true);

        TextRenderSetting style = TextRenderSetting.DEFAULT
                .withCustomColor(countColor)
                .withHorizontalAlignment(HorizontalAlignment.Right);
        return new TextRenderTask(String.valueOf(count), style);
    }

    private void drawTextOverlay(TextRenderTask task, ItemStack itemStack, int slotX, int slotY) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, 310); // gear box overlays are drawn at z301, so draw slightly above
        FontRenderer.getInstance().renderText(poseStack, slotX + 17, slotY + 9, task);

        // Restore vanilla count rendering
        WynnItem wynnItem = Models.Item.getWynnItem(itemStack).get();
        wynnItem.getCache().store(WynnItemCache.HIDE_COUNT_KEY, false);
    }
}

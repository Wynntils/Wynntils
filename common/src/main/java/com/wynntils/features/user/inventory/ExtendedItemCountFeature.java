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
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.type.Pair;
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

    // The text needs to be drawn on top of everything, so make sure we get the event
    // last
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderSlot(SlotRenderEvent.Post e) {
        if (!inventoryTextOverlayEnabled) return;

        drawTextOverlay(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Post e) {
        if (!hotbarTextOverlayEnabled) return;

        drawTextOverlay(e.getStack(), e.getX(), e.getY());
    }

    private void drawTextOverlay(ItemStack item, int slotX, int slotY) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(item);
        if (wynnItemOpt.isEmpty()) return;

        Pair<Integer, CustomColor> countInfo = getCountString(wynnItemOpt.get());
        if (countInfo == null) return;

        Integer count = countInfo.a();
        CustomColor countColor = countInfo.b();

        // This is a bit ugly; would rather we hid the drawing but that was tricky to do
        // with mixins...
        item.setCount(1);

        TextRenderSetting style = TextRenderSetting.DEFAULT
                .withCustomColor(countColor)
                .withHorizontalAlignment(HorizontalAlignment.Right);
        TextRenderTask task = new TextRenderTask(String.valueOf(count), style);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, 310); // gear box overlays are drawn at z301, so draw slightly above
        FontRenderer.getInstance().renderText(poseStack, slotX + 17, slotY + 9, task);
    }

    private Pair<Integer, CustomColor> getCountString(WynnItem wynnItem) {
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && wynnItem instanceof LeveledItemProperty leveledItem) {
            return Pair.of(leveledItem.getLevel(), CommonColors.WHITE);
        }

        if (wynnItem instanceof CountedItemProperty countedItem) {
            if (!countedItem.hasCount()) return null;
            return Pair.of(countedItem.getCount(), countedItem.getCountColor());
        }

        return null;
    }
}

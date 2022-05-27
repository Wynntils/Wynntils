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
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.utils.RenderUtils;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(performance = PerformanceImpact.SMALL, gameplay = GameplayImpact.MEDIUM, stability = Stability.STABLE)
public class ItemHighlightFeature extends FeatureBase {

    public ItemHighlightFeature() {
        setupEventListener();
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        ItemStack item = e.getSlot().getItem();
        if (!(item instanceof HighlightedItem highlightedItem)) return;

        int color = highlightedItem.getHighlightColor(e.getScreen(), e.getSlot());
        RenderUtils.drawTexturedRectWithColor(
                RenderUtils.highlight, color, e.getSlot().x - 1, e.getSlot().y - 1, 18, 18, 256, 256);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre e) {
        ItemStack item = e.getStack();
        if (!(item instanceof HotbarHighlightedItem highlightedItem)) return;

        int color = highlightedItem.getHotbarColor();
        RenderUtils.drawRect(color, e.getX(), e.getY(), 16, 16);
    }
}

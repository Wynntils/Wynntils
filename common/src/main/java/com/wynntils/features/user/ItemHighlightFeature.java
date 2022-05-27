/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.GameplayImpact;
import com.wynntils.core.features.properties.FeatureInfo.PerformanceImpact;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.utils.RenderUtils;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(performance = PerformanceImpact.SMALL, gameplay = GameplayImpact.MEDIUM, stability = Stability.STABLE)
public class ItemHighlightFeature extends UserFeature {
    public static ItemHighlightFeature INSTANCE;

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

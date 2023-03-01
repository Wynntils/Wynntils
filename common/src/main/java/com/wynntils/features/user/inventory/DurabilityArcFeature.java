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
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class DurabilityArcFeature extends UserFeature {
    @Config
    public boolean renderDurabilityArcInventories = true;

    @Config
    public boolean renderDurabilityArcHotbar = true;

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre e) {
        if (!renderDurabilityArcHotbar) return;
        drawDurabilityArc(e.getItemStack(), e.getX(), e.getY(), true);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!renderDurabilityArcInventories) return;
        drawDurabilityArc(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, false);
    }

    private void drawDurabilityArc(ItemStack itemStack, int slotX, int slotY, boolean hotbar) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof DurableItemProperty durableItem)) return;

        CappedValue durability = durableItem.getDurability();

        // calculate color of arc
        float durabilityFraction = (float) durability.current() / durability.max();
        int colorInt = Mth.hsvToRgb(Math.max(0f, durabilityFraction) / 3f, 1f, 1f);
        CustomColor color = CustomColor.fromInt(colorInt).withAlpha(160);

        // draw
        RenderUtils.drawArc(color, slotX, slotY, hotbar ? 0 : 200, durabilityFraction, 6, 8);
    }
}

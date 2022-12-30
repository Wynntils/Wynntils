/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.wynntils.core.components.Model;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.DurableItemProperty;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.INVENTORY)
public class DurabilityArcFeature extends UserFeature {
    @Config
    public boolean renderDurabilityArcInventories = true;

    @Config
    public boolean renderDurabilityArcHotbar = true;

    @Override
    public List<Model> getModelDependencies() {
        return List.of();
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre e) {
        if (!renderDurabilityArcHotbar) return;
        drawDurabilityArc(e.getStack(), e.getX(), e.getY(), true);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!renderDurabilityArcInventories) return;
        drawDurabilityArc(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, false);
    }

    private void drawDurabilityArc(ItemStack item, int slotX, int slotY, boolean hotbar) {
        Optional<ItemAnnotation> annotationOpt = ItemHandler.getItemStackAnnotation(item);
        if (annotationOpt.isEmpty()) return;
        if (!(annotationOpt.get() instanceof DurableItemProperty wynnItem)) return;

        CappedValue durability = wynnItem.getDurability();

        // calculate color of arc
        float durabilityFraction = (float) durability.getCurrent() / durability.getMax();
        int colorInt = Mth.hsvToRgb(Math.max(0f, durabilityFraction) / 3f, 1f, 1f);
        CustomColor color = CustomColor.fromInt(colorInt).withAlpha(160);

        // draw
        RenderUtils.drawArc(color, slotX, slotY, hotbar ? 0 : 200, durabilityFraction, 6, 8);
    }
}

/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.GetDurabilityBarColorEvent;
import com.wynntils.mc.event.GetDurabilityBarVisibilityEvent;
import com.wynntils.mc.event.GetDurabilityBarWidthEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class DurabilityOverlayFeature extends Feature {
    @Persisted
    public final Config<Boolean> renderDurabilityOverlayInventories = new Config<>(true);

    @Persisted
    public final Config<Boolean> renderDurabilityOverlayHotbar = new Config<>(true);

    @Persisted
    private final Config<DurabilityRenderMode> durabilityRenderMode = new Config<>(DurabilityRenderMode.ARC);

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.CountPre e) {
        if (!renderDurabilityOverlayHotbar.get()) return;
        if (durabilityRenderMode.get() != DurabilityRenderMode.ARC) return;
        drawDurabilityArc(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY());
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.CountPre e) {
        if (!renderDurabilityOverlayInventories.get()) return;
        if (durabilityRenderMode.get() != DurabilityRenderMode.ARC) return;
        drawDurabilityArc(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    @SubscribeEvent
    public void onGetDurabilityBarVisibility(GetDurabilityBarVisibilityEvent event) {
        boolean shouldBeVisible = false;

        if ((durabilityRenderMode.get() == DurabilityRenderMode.BAR)) {
            Optional<DurableItemProperty> durableItemProperty =
                    Models.Item.asWynnItemProperty(event.getStack(), DurableItemProperty.class);
            if (durableItemProperty.isPresent()) {
                int currentDurability =
                        durableItemProperty.get().getDurability().current();
                int maxDurability = durableItemProperty.get().getDurability().max();
                shouldBeVisible = currentDurability < maxDurability;
            }
        }

        event.setVisible(event.isVisible() || shouldBeVisible);
    }

    @SubscribeEvent
    public void onGetDurabilityBarWidth(GetDurabilityBarWidthEvent event) {
        event.setWidth(Models.Item.asWynnItemProperty(event.getStack(), DurableItemProperty.class)
                .map(durableItemProperty -> {
                    int currentDurability = durableItemProperty.getDurability().current();
                    int maxDurability = durableItemProperty.getDurability().max();
                    return Mth.clamp(Math.round(13.0F * (float) currentDurability / (float) maxDurability), 0, 13);
                })
                .orElse(event.getWidth()));
    }

    @SubscribeEvent
    public void onGetDurabilityBarColor(GetDurabilityBarColorEvent event) {
        event.setColor(Models.Item.asWynnItemProperty(event.getStack(), DurableItemProperty.class)
                .map(durableItemProperty -> {
                    int currentDurability = durableItemProperty.getDurability().current();
                    int maxDurability = durableItemProperty.getDurability().max();
                    float hueWynntils = Math.max(0.0F, (float) currentDurability / maxDurability) / 3.0F;
                    return Mth.hsvToRgb(hueWynntils, 1.0F, 1.0F);
                })
                .orElse(event.getColor()));
    }

    private void drawDurabilityArc(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemOpt =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty()) return;

        CappedValue durability = durableItemOpt.get().getDurability();

        // calculate color of arc
        float durabilityFraction = (float) durability.current() / durability.max();
        int colorInt = Mth.hsvToRgb(Math.max(0f, durabilityFraction) / 3f, 1f, 1f);
        CustomColor color = CustomColor.fromInt(colorInt).withAlpha(160);

        // draw
        RenderSystem.enableDepthTest();
        RenderUtils.drawArc(poseStack, color, slotX, slotY, 100, durabilityFraction, 6, 8);
        RenderSystem.disableDepthTest();
    }

    private enum DurabilityRenderMode {
        ARC,
        BAR;
    }
}

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
        if (!durabilityRenderMode.get().isArc()) return;
        drawDurabilityArc(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY());
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.CountPre e) {
        if (!renderDurabilityOverlayInventories.get()) return;
        if (!durabilityRenderMode.get().isArc()) return;
        drawDurabilityArc(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    public boolean isBarVisible(ItemStack stack) {
        if (!(durabilityRenderMode.get().isBar())) return false;

        Optional<DurableItemProperty> durableItemProperty =
                Models.Item.asWynnItemProperty(stack, DurableItemProperty.class);
        if (durableItemProperty.isEmpty()) return false;

        int currentDurability = durableItemProperty.get().getDurability().current();
        int maxDurability = durableItemProperty.get().getDurability().max();

        return currentDurability < maxDurability;
    }

    public Optional<Integer> getBarWidth(ItemStack stack) {
        return Models.Item.asWynnItemProperty(stack, DurableItemProperty.class).map(durableItemProperty -> {
            int currentDurability = durableItemProperty.getDurability().current();
            int maxDurability = durableItemProperty.getDurability().max();
            return Mth.clamp(Math.round(13.0F * (float) currentDurability / (float) maxDurability), 0, 13);
        });
    }

    public Optional<Integer> getBarColor(ItemStack stack) {
        return Models.Item.asWynnItemProperty(stack, DurableItemProperty.class).map(durableItemProperty -> {
            int currentDurability = durableItemProperty.getDurability().current();
            int maxDurability = durableItemProperty.getDurability().max();
            // This is Wynntils' way of calculating the color
            //      I had considered using Mojang's way, hence the comment
            float hueWynntils = Math.max(0.0F, (float) currentDurability / maxDurability) / 3.0F;
            return Mth.hsvToRgb(hueWynntils, 1.0F, 1.0F);
        });
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

        public boolean isArc() {
            return this == ARC;
        }

        public boolean isBar() {
            return this == BAR;
        }
    }
}

/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class DurabilityOverlayFeature extends Feature {
    @Persisted
    private final Config<Boolean> renderDurabilityOverlayInventories = new Config<>(true);

    @Persisted
    private final Config<Boolean> renderDurabilityOverlayHotbar = new Config<>(true);

    @Persisted
    private final Config<DurabilityRenderMode> durabilityRenderMode = new Config<>(DurabilityRenderMode.ARC);

    public DurabilityOverlayFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.CountPre e) {
        if (!renderDurabilityOverlayHotbar.get()) return;
        switch (durabilityRenderMode.get()) {
            case ARC -> drawDurabilityArc(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY());
            case BAR -> drawDurabilityBar(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY());
            case PERCENTAGE -> drawDurabilityPercentage(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY());
        }
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.CountPre e) {
        if (!renderDurabilityOverlayInventories.get()) return;
        switch (durabilityRenderMode.get()) {
            case ARC -> drawDurabilityArc(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
            case BAR -> drawDurabilityBar(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
            case PERCENTAGE ->
                drawDurabilityPercentage(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
        }
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

    private void drawDurabilityBar(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemProperty =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemProperty.isEmpty()) return;

        CappedValue durability = durableItemProperty.get().getDurability();

        if (durability.isAtCap()) return;

        // calculate width and hue
        int width = Mth.clamp(Math.round(13.0f * (float) durability.getProgress()), 0, 13);
        float hue = Math.max(0.0F, (float) durability.getProgress()) / 3.0F;

        // draw
        int i = slotX + 2;
        int j = slotY + 13;
        RenderUtils.drawRect(poseStack, CustomColor.fromInt(-16777216), i, j, 200, 13, 2);
        RenderUtils.drawRect(poseStack, CustomColor.fromHSV(hue, 1.0f, 1.0f, 1.0f), i, j, 200, width, 1);
    }

    // Inspiration taken from https://github.com/GTNewHorizons/DuraDisplay
    private void drawDurabilityPercentage(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemOpt =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty()) return;

        CappedValue durability = durableItemOpt.get().getDurability();

        float durabilityFraction = (float) durability.current() / durability.max();
        CustomColor color = CustomColor.fromHSV(Math.max(0.0f, durabilityFraction) / 3.0f, 1.0f, 1.0f, 1.0f);
        StyledText text = StyledText.fromString(Math.round(durabilityFraction * 100) + "%");

        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        text,
                        (float) slotX + 8,
                        (float) slotY + 16,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL,
                        0.5f,
                        Font.DisplayMode.NORMAL);
        poseStack.popPose();
    }

    private enum DurabilityRenderMode {
        ARC,
        BAR,
        PERCENTAGE
    }
}

/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class HeldItemNameOverlay extends Overlay {
    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    private final Config<Integer> messageDisplayTicks = new Config<>(40);

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    private final Config<Float> fontScale = new Config<>(1.0f);

    private StyledText itemText = StyledText.EMPTY;
    private int messageTimer;

    public HeldItemNameOverlay() {
        super(
                new OverlayPosition(
                        -49,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(300, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM);
    }

    @SubscribeEvent
    public void onRenderSelectedItemName(RenderEvent.Pre event) {
        if (shouldDisplayOriginal.get()) return;

        if (event.getType() == RenderEvent.ElementType.SELECTED_ITEM) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHeldItemChanged(ChangeCarriedItemEvent event) {
        ItemStack heldItem = McUtils.player().getMainHandItem();

        if (heldItem.isEmpty()) {
            messageTimer = 0;
            return;
        }

        itemText = StyledText.fromComponent(heldItem.getHoverName());
        messageTimer = messageDisplayTicks.get();
    }

    @Override
    protected void tick() {
        if (messageTimer <= 0) return;

        messageTimer--;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (messageTimer <= 0 || itemText.isEmpty()) return;

        int alpha = (int) Math.min((float) messageTimer * 256.0F / 10.0F, 255.0F);
        if (alpha <= 0) return;

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        itemText,
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        this.getRenderY(),
                        this.getRenderY() + this.getHeight(),
                        this.getWidth(),
                        CommonColors.WHITE.withAlpha(alpha),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        textShadow.get(),
                        fontScale.get());
    }

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        StyledText.fromString("Content Book"),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        this.getRenderY(),
                        this.getRenderY() + this.getHeight(),
                        this.getWidth(),
                        CustomColor.fromChatFormatting(ChatFormatting.LIGHT_PURPLE),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        textShadow.get(),
                        fontScale.get());
    }
}

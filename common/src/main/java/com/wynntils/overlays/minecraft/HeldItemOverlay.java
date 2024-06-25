/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minecraft;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.MinecraftOverlay;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeldItemOverlay extends Overlay implements MinecraftOverlay {
    @Persisted
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    public final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted
    public final Config<Boolean> hideHeldItemName = new Config<>(false);

    private static final int MESSAGE_TICKS = 40;

    private int messageTimer;
    private ItemStack heldItem = ItemStack.EMPTY;
    private StyledText message;

    public HeldItemOverlay() {
        // It's not 100% exact but it's close enough to the vanilla position
        super(
                new OverlayPosition(
                        -45,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(200, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @SubscribeEvent
    public void onRender(RenderEvent.Pre event) {
        // Always cancel vanilla rendering
        if (event.getType() == RenderEvent.ElementType.HELD_ITEM_NAME) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        ItemStack currentItem = InventoryUtils.getItemInHand();

        if (!ItemStack.matches(currentItem, heldItem)) {
            heldItem = currentItem;

            if (currentItem.getItem() == Items.AIR) {
                message = StyledText.fromString("");
            } else {
                // FIXME: Italics don't render
                message = StyledText.fromComponent(heldItem.getHoverName());
                messageTimer = MESSAGE_TICKS;
            }

            return;
        }

        if (messageTimer <= 0) return;

        messageTimer--;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        if (hideHeldItemName.get()) return;
        if (messageTimer <= 0) return;

        // Render it the same way vanilla renders item changes
        int alpha = (int) Math.min((float) messageTimer * 256.0F / 10.0F, 255.0F);
        if (alpha <= 0) return;

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        message,
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
    public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        int alpha = (int) Math.min((float) MESSAGE_TICKS * 256.0F / 10.0F, 255.0F);

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        StyledText.fromComponent(InventoryUtils.getItemInHand().getHoverName()),
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
    protected void onConfigUpdate(Config<?> config) {}
}

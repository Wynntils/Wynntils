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
import com.wynntils.mc.event.ActionBarMessageSetEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionBarOverlay extends Overlay implements MinecraftOverlay {
    @Persisted
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    public final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted
    public final Config<Boolean> hideActionBar = new Config<>(false);

    private static final int MESSAGE_TICKS = 60;

    private int messageTimer;
    private StyledText message;

    public ActionBarOverlay() {
        // It's not 100% exact but it's close enough to the vanilla position
        super(
                new OverlayPosition(
                        -57,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(200, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (messageTimer <= 0) return;

        messageTimer--;
    }

    @SubscribeEvent
    public void onActionBarMessageSet(ActionBarMessageSetEvent event) {
        message = StyledText.fromComponent(event.getMessage());
        messageTimer = MESSAGE_TICKS;

        // Set the vanilla action bar to empty
        event.setMessage(Component.empty());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        if (hideActionBar.get()) return;
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
                        StyledText.fromString("§2[§a|||Sprint|||§2]"),
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

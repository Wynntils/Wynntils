/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.StyledText;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * An overlay, which main purpose is to display function templates.
 */
public abstract class TextOverlay extends DynamicOverlay {
    @RegisterConfig("overlay.wynntils.textOverlay.textShadow")
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    protected StyledText[] cachedLines = new StyledText[0];

    protected TextOverlay(OverlayPosition position, float width, float height) {
        super(position, width, height, 1);
    }

    protected TextOverlay(OverlayPosition position, OverlaySize size) {
        super(position, size, 1);
    }

    protected TextOverlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride) {
        super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride, 1);
    }

    protected TextOverlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride,
            int id) {
        super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride, id);
    }

    protected TextOverlay(int id) {
        super(id);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        if (!Models.WorldState.onWorld()) return;

        renderTemplate(poseStack, bufferSource, cachedLines, getTextScale());
    }

    @Override
    public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        if (!Models.WorldState.onWorld()) return;

        renderTemplate(poseStack, bufferSource, calculateTemplateValue(getPreviewTemplate()), getTextScale());
    }

    protected void renderTemplate(
            PoseStack poseStack, MultiBufferSource bufferSource, StyledText[] lines, float textScale) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        for (StyledText line : lines) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            line,
                            renderX,
                            renderX + this.getWidth(),
                            renderY,
                            renderY + this.getHeight(),
                            0,
                            this.getRenderColor(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            this.textShadow.get(),
                            textScale);

            renderY += FontRenderer.getInstance().getFont().lineHeight;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;

        cachedLines = calculateTemplateValue(getTemplate());
    }

    protected StyledText[] calculateTemplateValue(String template) {
        return Managers.Function.doFormatLines(template);
    }

    public CustomColor getRenderColor() {
        return CommonColors.WHITE;
    }

    public float getTextScale() {
        return 1f;
    }

    public abstract String getTemplate();

    public abstract String getPreviewTemplate();

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {}
}

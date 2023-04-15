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
import com.wynntils.core.text.CodedString;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public abstract class BarOverlay extends DynamicOverlay {
    @RegisterConfig("overlay.wynntils.barOverlay.textShadow")
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @RegisterConfig("overlay.wynntils.barOverlay.flip")
    public final Config<Boolean> flip = new Config<>(false);

    @RegisterConfig("overlay.wynntils.barOverlay.animationTime")
    public final Config<Float> animationTime = new Config<>(2f);

    @RegisterConfig("overlay.wynntils.barOverlay.heightModifier")
    public final Config<Float> heightModifier = new Config<>(1f);

    private Pair<CodedString, ErrorOr<CappedValue>> templateCache;

    protected float currentProgress = 0f;

    protected BarOverlay(int id, OverlaySize overlaySize) {
        super(id);

        this.size.updateConfig(overlaySize);
    }

    protected BarOverlay(OverlayPosition position, OverlaySize size) {
        super(position, size, 1);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        if (!isRendered()) return;

        BarOverlayTemplatePair template = getTemplate();

        if (templateCache == null) {
            templateCache = calculateTemplate(template);
        }

        ErrorOr<CappedValue> valueOrError = templateCache.value();
        if (valueOrError.hasError()) {
            renderText(
                    poseStack, bufferSource, getModifiedRenderY(10), CodedString.fromString(valueOrError.getError()));
            return;
        }

        render(poseStack, bufferSource, currentProgress, templateCache.key());
    }

    @Override
    public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        BarOverlayTemplatePair previewTemplate = getPreviewTemplate();
        Pair<CodedString, ErrorOr<CappedValue>> calculatedTemplate = calculateTemplate(previewTemplate);

        ErrorOr<CappedValue> valueOrError = calculatedTemplate.value();
        if (valueOrError.hasError()) {
            renderText(
                    poseStack, bufferSource, getModifiedRenderY(10), CodedString.fromString(valueOrError.getError()));
            return;
        }

        // Do not render bars that has no value
        if (valueOrError.getValue().equals(CappedValue.EMPTY)) return;

        render(poseStack, bufferSource, (float) valueOrError.getValue().getProgress(), calculatedTemplate.key());
    }

    protected void render(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderedProgress, CodedString textValue) {
        float barHeight = getTextureHeight() * heightModifier.get();
        float renderY = getModifiedRenderY(barHeight + 10);

        renderText(poseStack, bufferSource, renderY, textValue);

        float progress = (flip.get() ? -1 : 1) * renderedProgress;
        renderBar(poseStack, bufferSource, renderY + 10, barHeight, progress);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld() || !isRendered()) return;

        BarOverlayTemplatePair template = getTemplate();

        templateCache = calculateTemplate(template);

        if (templateCache.b().hasError()) return;

        CappedValue value = templateCache.b().getValue();

        if (value == CappedValue.EMPTY) return;

        if (animationTime.get() == 0) {
            currentProgress = (float) value.getProgress();
            return;
        }

        currentProgress -= (animationTime.get() * 0.1f) * (currentProgress - value.getProgress());
    }

    private Pair<CodedString, ErrorOr<CappedValue>> calculateTemplate(BarOverlayTemplatePair template) {
        return Pair.of(
                CodedString.join(" ", Managers.Function.doFormatLines(template.textTemplate)),
                Managers.Function.tryGetRawValueOfType(template.valueTemplate, CappedValue.class));
    }

    protected abstract float getTextureHeight();

    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        Texture texture = getTexture();

        if (getRenderColor() == CommonColors.WHITE) {
            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    texture,
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + renderHeight,
                    0,
                    0,
                    texture.width(),
                    texture.height(),
                    progress);
        } else {
            BufferedRenderUtils.drawColoredProgressBar(
                    poseStack,
                    bufferSource,
                    texture,
                    getRenderColor(),
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + renderHeight,
                    0,
                    0,
                    texture.width(),
                    texture.height(),
                    progress);
        }
    }

    protected void renderText(PoseStack poseStack, MultiBufferSource bufferSource, float renderY, CodedString text) {
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        text,
                        getRenderX(),
                        getRenderX() + getWidth(),
                        renderY,
                        0,
                        getRenderColor(),
                        getRenderHorizontalAlignment(),
                        textShadow.get());
    }

    protected float getModifiedRenderY(float renderedHeight) {
        return switch (this.getRenderVerticalAlignment()) {
            case TOP -> this.getRenderY();
            case MIDDLE -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
            case BOTTOM -> this.getRenderY() + this.getHeight() - renderedHeight;
        };
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {}

    public abstract boolean isRendered();

    public abstract Texture getTexture();

    public CustomColor getRenderColor() {
        return CommonColors.WHITE;
    }

    public float getTextScale() {
        return 1f;
    }

    public abstract BarOverlayTemplatePair getTemplate();

    public abstract BarOverlayTemplatePair getPreviewTemplate();

    public record BarOverlayTemplatePair(String textTemplate, String valueTemplate) {}
}

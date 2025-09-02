/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.UniversalTexture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class BaseBarOverlay extends Overlay {
    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.textShadow")
    protected final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.flip")
    private final Config<Boolean> flip = new Config<>(false);

    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.barTexture")
    protected final Config<UniversalTexture> barTexture = new Config<>(UniversalTexture.A);

    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.animationTime")
    private final Config<Float> animationTime = new Config<>(2f);

    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.shouldDisplayOriginal")
    protected final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    // hacky override of custom color
    @Persisted(i18nKey = "feature.wynntils.gameBarsOverlay.overlay.baseBar.textColor")
    protected final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

    protected float currentProgress = 0f;

    protected BaseBarOverlay(OverlayPosition position, OverlaySize size, CustomColor textColor) {
        super(position, size);
        this.textColor.store(textColor);
        WynntilsMod.registerListener(this::onBossBarAdd);
    }

    protected float textureHeight() {
        return barTexture.get().getHeight();
    }

    protected abstract BossBarProgress progress();

    protected abstract Class<? extends TrackedBar> getTrackedBarClass();

    // As this is an abstract class, this event was subscribed to manually in ctor
    private void onBossBarAdd(BossBarAddedEvent event) {
        if (!Managers.Overlay.isEnabled(this)) return;
        if (!event.getTrackedBar().getClass().equals(getTrackedBarClass())) return;

        if (!shouldDisplayOriginal.get()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void tick() {
        if (!isRendered() || progress() == null) return;

        if (animationTime.get() == 0) {
            currentProgress = progress().progress();
            return;
        }

        currentProgress -=
                (animationTime.get() * 0.1f) * (currentProgress - progress().progress());
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        float renderedProgress = Math.round((flip.get() ? -1 : 1) * currentProgress * 100) / 100f;
        renderAll(guiGraphics, bufferSource, renderedProgress);
    }

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (progress() == null) {
            renderAll(guiGraphics, bufferSource, 50);
            return;
        }
        float renderedProgress = Math.round((flip.get() ? -1 : 1) * currentProgress * 100) / 100f;
        renderAll(guiGraphics, bufferSource, renderedProgress);
    }

    private void renderAll(GuiGraphics guiGraphics, MultiBufferSource bufferSource, float renderedProgress) {
        PoseStack poseStack = guiGraphics.pose();

        float barHeight = textureHeight() * (this.getWidth() / 81);
        float renderY = getModifiedRenderY(barHeight + 10);

        renderText(poseStack, bufferSource, renderY, text());

        renderBar(poseStack, bufferSource, renderY + 10, barHeight, renderedProgress);
    }

    protected String text() {
        BossBarProgress barProgress = progress();
        if (progress() == null) {
            return icon();
        }
        return String.format(
                "%s %s %s",
                progress().value().current(), icon(), progress().value().max());
    }

    protected String icon() {
        return "";
    }

    private float getModifiedRenderY(float renderedHeight) {
        return switch (this.getRenderVerticalAlignment()) {
            case TOP -> this.getRenderY();
            case MIDDLE -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
            case BOTTOM -> this.getRenderY() + this.getHeight() - renderedHeight;
        };
    }

    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        BufferedRenderUtils.drawColoredProgressBar(
                poseStack,
                bufferSource,
                Texture.UNIVERSAL_BAR,
                this.textColor.get(),
                getRenderX(),
                renderY,
                getRenderX() + getWidth(),
                renderY + renderHeight,
                0,
                barTexture.get().getTextureY1(),
                Texture.UNIVERSAL_BAR.width(),
                barTexture.get().getTextureY2(),
                progress);
    }

    protected void renderText(PoseStack poseStack, MultiBufferSource bufferSource, float renderY, String text) {
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        StyledText.fromString(text),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }
}

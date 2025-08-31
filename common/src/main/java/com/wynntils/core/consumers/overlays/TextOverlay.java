/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * An overlay, which main purpose is to display function templates.
 */
public abstract class TextOverlay extends DynamicOverlay {
    @Persisted(i18nKey = "overlay.wynntils.textOverlay.textShadow")
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted(i18nKey = "overlay.wynntils.textOverlay.fontScale")
    protected final Config<Float> fontScale = new Config<>(1.0f);

    private StyledText[] cachedLines = new StyledText[0];

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
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        renderTemplate(guiGraphics, bufferSource, cachedLines, getTextScale());
    }

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        renderTemplate(guiGraphics, bufferSource, calculateTemplateValue(getPreviewTemplate()), getTextScale());
    }

    private void renderTemplate(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, StyledText[] lines, float textScale) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        lines,
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
    }

    @Override
    public void tick() {
        if (!isRendered()) return;
        cachedLines = calculateTemplateValue(getTemplate());
    }

    @Override
    protected boolean isVisible() {
        return !getTemplate().isEmpty();
    }

    protected StyledText[] calculateTemplateValue(String template) {
        return Managers.Function.doFormatLines(template);
    }

    protected CustomColor getRenderColor() {
        return CommonColors.WHITE;
    }

    private float getTextScale() {
        return fontScale.get();
    }

    protected abstract String getTemplate();

    protected abstract String getPreviewTemplate();
}

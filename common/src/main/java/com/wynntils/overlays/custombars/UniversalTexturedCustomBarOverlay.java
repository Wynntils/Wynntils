/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.UniversalTexture;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;

public class UniversalTexturedCustomBarOverlay extends CustomBarOverlayBase {
    @Persisted
    private final Config<UniversalTexture> barTexture = new Config<>(UniversalTexture.A);

    @Persisted
    private final Config<String> colorTemplate = new Config<>("");

    private ErrorOr<CustomColor> colorCache = ErrorOr.of(CommonColors.WHITE);

    public UniversalTexturedCustomBarOverlay(int id) {
        super(id, new OverlaySize(81, 21));
    }

    @Override
    public CustomColor getRenderColor() {
        return colorCache.hasError() ? CommonColors.WHITE : colorCache.getValue();
    }

    @Override
    public Texture getTexture() {
        return Texture.UNIVERSAL_BAR;
    }

    @Override
    protected float getTextureHeight() {
        return barTexture.get().getHeight();
    }

    @Override
    protected void renderOrErrorMessage(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (colorCache.hasError()) {
            StyledText[] errorMessage = {
                StyledText.fromString("§c§l"
                        + I18n.get(
                                "feature.wynntils.customBarsOverlay.overlay.universalTexturedCustomBar.colorTemplate.error")
                        + " " + getTranslatedName()),
                StyledText.fromUnformattedString(colorCache.getError())
            };
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            bufferSource,
                            errorMessage,
                            getRenderX(),
                            getRenderX() + getWidth(),
                            getRenderY(),
                            getRenderY() + getHeight(),
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1);

        } else {
            super.renderOrErrorMessage(guiGraphics, bufferSource, deltaTracker, window);
        }
    }

    @Override
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        BufferedRenderUtils.drawColoredProgressBar(
                poseStack,
                bufferSource,
                Texture.UNIVERSAL_BAR,
                colorCache.getValue(),
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

    @Override
    public void tick() {
        super.tick();

        // If the color template is empty, use white as the default
        String template = colorTemplate.get();
        if (template.isEmpty()) {
            colorCache = ErrorOr.of(CommonColors.WHITE);
            return;
        }

        // Get the color from the template
        String formattedTemplate =
                StyledText.join("", Managers.Function.doFormatLines(template)).getString();
        colorCache = Managers.Function.tryGetRawValueOfType(formattedTemplate, CustomColor.class);
    }

    @Override
    protected BarOverlayTemplatePair getActualPreviewTemplate() {
        return new BarOverlayTemplatePair("3/10", "capped(3; 10)");
    }
}

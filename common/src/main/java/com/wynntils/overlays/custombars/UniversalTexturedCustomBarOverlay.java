/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.UniversalTexture;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.renderer.MultiBufferSource;

public class UniversalTexturedCustomBarOverlay extends CustomBarOverlayBase {
    @Persisted
    public final Config<UniversalTexture> barTexture = new Config<>(UniversalTexture.A);

    @Persisted
    public final Config<String> colorTemplate = new Config<>("");

    public UniversalTexturedCustomBarOverlay(int id) {
        super(id, new OverlaySize(81, 21));
    }

    @Override
    public CustomColor getRenderColor() {
        // If the color template is empty, use white as the default
        String template = colorTemplate.get();
        if (template.isEmpty()) return CommonColors.WHITE;

        // Get the color from the template
        String formattedTemplate =
                StyledText.join("", Managers.Function.doFormatLines(template)).getString();
        ErrorOr<CustomColor> colorOrError =
                Managers.Function.tryGetRawValueOfType(formattedTemplate, CustomColor.class);
        // If there is an error, use white
        if (colorOrError.hasError()) return CommonColors.WHITE;
        return colorOrError.getValue();
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
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        BufferedRenderUtils.drawColoredProgressBar(
                poseStack,
                bufferSource,
                Texture.UNIVERSAL_BAR,
                getRenderColor(),
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
    protected BarOverlayTemplatePair getActualPreviewTemplate() {
        return new BarOverlayTemplatePair("3/10", "capped(3; 10)");
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.ObjectivesTextures;
import net.minecraft.client.renderer.MultiBufferSource;

public class BubbleTexturedCustomBarOverlay extends CustomBarOverlayBase {
    @RegisterConfig("overlay.wynntils.objectivesTexture")
    public final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.A);

    public BubbleTexturedCustomBarOverlay(int id) {
        super(id, new OverlaySize(150, 30));
    }

    @Override
    public Texture getTexture() {
        return Texture.BUBBLE_BAR;
    }

    @Override
    protected float getTextureHeight() {
        return 5;
    }

    @Override
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float barHeight, float progress) {
        BufferedRenderUtils.drawProgressBar(
                poseStack,
                bufferSource,
                getTexture(),
                getRenderX(),
                renderY,
                getRenderX() + getWidth(),
                renderY + barHeight,
                0,
                objectivesTexture.get().getTextureYOffset(),
                182,
                objectivesTexture.get().getTextureYOffset() + 10,
                progress);
    }

    @Override
    protected BarOverlayTemplatePair getActualPreviewTemplate() {
        return new BarOverlayTemplatePair("{capped_ingredient_pouch_slots}", "capped_ingredient_pouch_slots");
    }
}

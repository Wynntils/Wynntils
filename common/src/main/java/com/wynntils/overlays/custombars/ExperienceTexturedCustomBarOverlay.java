/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.ObjectivesTextures;
import net.minecraft.client.gui.GuiGraphics;

public class ExperienceTexturedCustomBarOverlay extends CustomBarOverlayBase {
    @Persisted(i18nKey = "overlay.wynntils.objectivesTexture")
    private final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.A);

    public ExperienceTexturedCustomBarOverlay(int id) {
        super(id, new OverlaySize(150, 30));
    }

    @Override
    protected Texture getTexture() {
        return Texture.EXPERIENCE_BAR;
    }

    @Override
    protected float getTextureHeight() {
        return 5;
    }

    @Override
    protected void renderBar(GuiGraphics guiGraphics, float renderY, float barHeight, float progress) {
        RenderUtils.drawProgressBar(
                guiGraphics,
                getTexture(),
                getRenderX(),
                renderY,
                getRenderX() + getWidth(),
                renderY + barHeight,
                0,
                objectivesTexture.get().getTextureY1(),
                Texture.EXPERIENCE_BAR.width(),
                objectivesTexture.get().getTextureY2(),
                progress);
    }

    @Override
    protected BarOverlayTemplatePair getActualPreviewTemplate() {
        return new BarOverlayTemplatePair("{capped_xp}", "capped_xp");
    }
}

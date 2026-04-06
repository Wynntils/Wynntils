/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.DistortionBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;

public class DistortionBarOverlay extends BaseBarOverlay {
    @Persisted
    private final Config<CustomColor> activatedColor = new Config<>(CustomColor.fromHexString("d599ffff"));

    public DistortionBarOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.PURPLE);
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.distortionBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return DistortionBar.class;
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.distortionBar.isActive();
    }

    @Override
    protected String text() {
        return "Distortion: " + Models.Ability.distortionBar.getCurrent();
    }

    @Override
    protected void renderBar(GuiGraphics guiGraphics, float renderY, float renderHeight, float progress) {
        Texture universalBarTexture = Texture.UNIVERSAL_BAR;

        RenderUtils.drawColoredProgressBar(
                guiGraphics,
                universalBarTexture,
                Models.Ability.distortionBar.getCurrent() > 0 ? this.activatedColor.get() : this.textColor.get(),
                this.getRenderX(),
                renderY,
                this.getRenderX() + this.getWidth(),
                renderY + renderHeight,
                0,
                barTexture.get().getTextureY1(),
                universalBarTexture.width(),
                barTexture.get().getTextureY2(),
                progress);
    }

    @Override
    protected void renderText(GuiGraphics guiGraphics, float renderY, String text) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(text),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        Models.Ability.distortionBar.getCurrent() > 0
                                ? this.activatedColor.get()
                                : this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }
}

/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

public class CommanderBarOverlay extends BaseBarOverlay {
    @Persisted
    private final Config<CustomColor> activatedColor = new Config<>(CommonColors.GREEN);

    public CommanderBarOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.RED);
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.commanderBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return CommanderBar.class;
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.commanderBar.isActive();
    }

    @Override
    protected String text() {
        return "Commander: " + Models.Ability.commanderBar.getDuration() + "s";
    }

    @Override
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        Texture universalBarTexture = Texture.UNIVERSAL_BAR;

        BufferedRenderUtils.drawColoredProgressBar(
                poseStack,
                bufferSource,
                universalBarTexture,
                Models.Ability.commanderBar.isActivated() ? this.activatedColor.get() : this.textColor.get(),
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
                        Models.Ability.commanderBar.isActivated() ? this.activatedColor.get() : this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }
}

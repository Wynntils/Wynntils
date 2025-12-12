/*
 * Copyright © Wynntils 2025.
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
import com.wynntils.models.abilities.bossbars.MomentumBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class MomentumBarOverlay extends BaseBarOverlay {
    @Persisted
    private final Config<CustomColor> maximumColor = new Config<>(CommonColors.GREEN);

    public MomentumBarOverlay() {
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
        return Models.Ability.momentumBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return MomentumBar.class;
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.momentumBar.isActive();
    }

    @Override
    protected void renderBar(GuiGraphics guiGraphics, float renderY, float renderHeight, float progress) {
        RenderUtils.drawColoredProgressBar(
                guiGraphics,
                Texture.UNIVERSAL_BAR,
                Models.Ability.momentumBar.isMax() ? this.maximumColor.get() : this.textColor.get(),
                this.getRenderX(),
                renderY,
                this.getRenderX() + this.getWidth(),
                renderY + renderHeight,
                0,
                barTexture.get().getTextureY1(),
                Texture.UNIVERSAL_BAR.width(),
                barTexture.get().getTextureY2(),
                progress);
    }

    @Override
    protected void renderText(GuiGraphics guiGraphics, float renderY, String text) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(Models.Ability.momentumBar.getMomentum() + " ")
                                .append(StyledText.fromComponent(Component.literal("\uE013")
                                        .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(
                                                Identifier.withDefaultNamespace("common")))))),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        Models.Ability.momentumBar.isMax() ? this.maximumColor.get() : this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }
}

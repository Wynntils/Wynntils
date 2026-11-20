/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynntilsMountTraitFont;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class MountTraitsOverlay extends Overlay {
    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    private static final Identifier ENERGY_TEXTURE_IDENTIFIER =
            Identifier.withDefaultNamespace("textures/font/hud/gameplay/default/center_left/mount_energy.png");
    private static final Identifier TOUGHNESS_TEXTURE_IDENTIFIER =
            Identifier.withDefaultNamespace("textures/font/hud/gameplay/default/center_left/mount_toughness.png");
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 144;
    private static final int FRAME_WIDTH = 8;
    private static final int FRAME_HEIGHT = 48;
    private static final int BAR_GAP = 2;
    private static final int ICON_GAP = 2;
    private static final int ICON_HEIGHT = 16;
    private static final int DEFAULT_WIDTH = FRAME_WIDTH * 2 + BAR_GAP;
    private static final int FRAMES_PER_ROW = TEXTURE_WIDTH / FRAME_WIDTH;
    private static final int EMPTY_FRAME = 47;

    private int energyFrame;
    private int toughnessFrame;

    public MountTraitsOverlay() {
        super(
                new OverlayPosition(
                        // Place it just below the minimap to prevent overlap
                        140,
                        11,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(DEFAULT_WIDTH, FRAME_HEIGHT + ICON_GAP + ICON_HEIGHT));
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Mount.setHideMountTraits(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, Window window) {
        renderTraits(guiGraphics, energyFrame, toughnessFrame);
    }

    @Override
    public void renderPreview(GuiGraphicsExtractor guiGraphics, Window window) {
        renderTraits(guiGraphics, 16, 31);
    }

    private void renderTraits(GuiGraphicsExtractor guiGraphics, int energyFrame, int toughnessFrame) {
        float renderX = this.getRenderX();
        renderBar(guiGraphics, ENERGY_TEXTURE_IDENTIFIER, renderX, energyFrame);
        renderBar(guiGraphics, TOUGHNESS_TEXTURE_IDENTIFIER, renderX + FRAME_WIDTH + BAR_GAP, toughnessFrame);
        renderIcon(guiGraphics, "energy", renderX + FRAME_WIDTH / 2f);
        renderIcon(guiGraphics, "toughness", renderX + FRAME_WIDTH + BAR_GAP + FRAME_WIDTH / 2f + 1);
    }

    private void renderIcon(GuiGraphicsExtractor guiGraphics, String glyph, float centerX) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(WynnFont.asFont(glyph, WynntilsMountTraitFont.class)),
                        centerX,
                        getRenderY() + FRAME_HEIGHT + ICON_GAP,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private void renderBar(GuiGraphicsExtractor guiGraphics, Identifier texture, float x, int frame) {
        RenderUtils.drawTexturedRect(
                guiGraphics,
                texture,
                x,
                getRenderY(),
                FRAME_WIDTH,
                FRAME_HEIGHT,
                (frame % FRAMES_PER_ROW) * FRAME_WIDTH,
                (frame / FRAMES_PER_ROW) * FRAME_HEIGHT,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    @Override
    public boolean isVisible() {
        return Models.Mount.getCurrentMountEnergy().isPresent()
                && Models.Mount.getCurrentMountToughness().isPresent();
    }

    @Override
    public void tick() {
        energyFrame = Models.Mount.getCurrentMountEnergy()
                .map(CappedValue::getRemaining)
                .orElse(EMPTY_FRAME);
        toughnessFrame = Models.Mount.getCurrentMountToughness()
                .map(CappedValue::getRemaining)
                .orElse(EMPTY_FRAME);
    }
}

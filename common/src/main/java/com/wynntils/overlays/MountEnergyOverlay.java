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
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class MountEnergyOverlay extends Overlay {
    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    private static final Identifier TEXTURE_IDENTIFIER =
            Identifier.withDefaultNamespace("textures/font/hud/gameplay/default/center_left/mount_energy.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 192;
    private static final int FRAME_WIDTH = 16;
    private static final int FRAME_HEIGHT = 64;
    private static final int TOTAL_FRAMES = 48;

    private int uOffset = 0;
    private int vOffset = 0;

    public MountEnergyOverlay() {
        super(
                new OverlayPosition(
                        // Place it just below the minimap to prevent overlap
                        140,
                        8,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(FRAME_WIDTH, FRAME_HEIGHT));
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Mount.setHideMountEnergy(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        RenderUtils.drawTexturedRect(
                guiGraphics,
                TEXTURE_IDENTIFIER,
                renderX,
                renderY,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                uOffset,
                vOffset,
                FRAME_WIDTH,
                FRAME_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    @Override
    public boolean isVisible() {
        return Models.Mount.getCurrentMountEnergy().isPresent();
    }

    @Override
    public void tick() {
        if (Models.Mount.getCurrentMountEnergy().isEmpty()) {
            uOffset = 0;
            vOffset = 0;
            return;
        }

        CappedValue mountEnergy = Models.Mount.getCurrentMountEnergy().get();
        int frame = Math.round((float) ((1.0 - mountEnergy.getProgress()) * (TOTAL_FRAMES - 1)));

        uOffset = (frame % FRAME_WIDTH) * FRAME_WIDTH;
        vOffset = (frame / FRAME_WIDTH) * FRAME_HEIGHT;
    }
}

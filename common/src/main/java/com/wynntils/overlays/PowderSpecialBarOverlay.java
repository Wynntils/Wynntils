/*
 * Copyright © Wynntils 2023-2026.
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
import com.wynntils.models.characterstats.type.PowderSpecialInfo;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class PowderSpecialBarOverlay extends Overlay {
    private static final Identifier POWDER_SPECIAL_BAR_TEXTURE =
            Identifier.withDefaultNamespace("textures/font/hud/gameplay/default/center_middle/powder_special.png");
    private static final int TEXTURE_WIDTH = 320;
    private static final int TEXTURE_HEIGHT = 192;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_HEIGHT = 6;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 32;
    private static final int U_OFFSET = 10;
    private static final int V_OFFSET = 13;

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    public PowderSpecialBarOverlay() {
        super(
                new OverlayPosition(
                        5,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.MIDDLE),
                new OverlaySize(BAR_WIDTH, BAR_HEIGHT));
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        Optional<PowderSpecialInfo> powderSpecialInfoOpt = Models.CharacterStats.getPowderSpecialInfo();
        if (powderSpecialInfoOpt.isEmpty()) return;

        renderWithSpecificSpecial(
                guiGraphics,
                powderSpecialInfoOpt.get().charge() * 10f,
                powderSpecialInfoOpt.get().powder());
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        renderWithSpecificSpecial(guiGraphics, 4, Powder.THUNDER);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.CharacterStats.setHidePowder(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    private void renderWithSpecificSpecial(
            GuiGraphics guiGraphics, float powderSpecialCharge, Powder powderSpecialType) {
        RenderUtils.drawTexturedRect(
                guiGraphics,
                POWDER_SPECIAL_BAR_TEXTURE,
                getRenderX(),
                getRenderY(),
                BAR_WIDTH,
                BAR_HEIGHT,
                U_OFFSET + (CELL_WIDTH * (Math.max(0, powderSpecialCharge - 1))),
                calculateVOffset(powderSpecialType),
                BAR_WIDTH,
                BAR_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    private int calculateVOffset(Powder powderSpecialType) {
        int powderOrder = powderSpecialType == null
                ? 0
                : switch (powderSpecialType) {
                    case AIR -> 1;
                    case EARTH -> 2;
                    case FIRE -> 3;
                    case THUNDER -> 4;
                    case WATER -> 5;
                };

        return V_OFFSET + (CELL_HEIGHT * powderOrder);
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class DialogueOverlay extends Overlay {
    @Persisted
    public final Config<Float> scale = new Config<>(1f);

    public DialogueOverlay() {
        super(
                new OverlayPosition(
                        -9,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(200, 100));
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        if (Models.Dialogue.isDialoguePresent()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            Models.Dialogue.getCurrentDialogue(),
                            getRenderX(),
                            getRenderX() + getWidth(),
                            getRenderY(),
                            getRenderY() + getHeight(),
                            0,
                            CommonColors.WHITE,
                            getRenderHorizontalAlignment(),
                            getRenderVerticalAlignment(),
                            TextShadow.NORMAL,
                            scale.get());
        }
    }
}

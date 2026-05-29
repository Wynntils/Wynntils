/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;

public class MountEnergyOverlay extends TextOverlay {
    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    public MountEnergyOverlay() {
        super(
                new OverlayPosition(
                        // Doesn't mimic the vanilla position that well but it's good enough
                        (float) (McUtils.guiScale() * 3.3),
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                new OverlaySize(20, 70));
        textShadow.store(TextShadow.NONE);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Mount.setHideMountEnergy(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    protected String getTemplate() {
        return "{with_font(styled_text(from_codepoint(subtract(57391;current(current_mount_energy))));\"hud/gameplay/default/center_left\")}";
    }

    @Override
    protected String getPreviewTemplate() {
        return "{with_font(styled_text(\"\uE017\");\"hud/gameplay/default/center_left\")}";
    }

    @Override
    protected void renderTemplate(GuiGraphics guiGraphics, StyledText[] lines, float textScale) {
        // Offset the text to counter the offset from the font
        float renderX = this.getRenderX() + getXOffset();
        float renderY = this.getRenderY() + getYOffset();
        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        guiGraphics,
                        lines,
                        renderX,
                        renderX + this.getWidth(),
                        renderY,
                        renderY + this.getHeight(),
                        fitText.get() ? this.getWidth() : 0,
                        this.backgroundBorderWidth.get(),
                        this.getRenderColor(),
                        this.backgroundColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        this.textShadow.get(),
                        textScale);
    }

    @Override
    public boolean isVisible() {
        return Models.Mount.getCurrentMountEnergy().isPresent();
    }

    private float getXOffset() {
        double guiScale = McUtils.guiScale();
        return (float) (960.0 / guiScale + 2.0);
    }

    private float getYOffset() {
        double guiScale = McUtils.guiScale();
        return (float) (504.0 / guiScale - 28.0);
    }
}

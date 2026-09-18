/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class OverlayHelpPanel {
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_COLOR = 0xFF3E2418;

    private final List<Component> lines;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int visibleLines;
    private int scrollOffset;

    OverlayHelpPanel(List<Component> content, int screenWidth, int screenHeight, boolean buttonsAtBottom) {
        width = Math.min(270, Math.max(40, screenWidth - 16));
        lines = ComponentUtils.wrapTooltips(content, width - 28);
        visibleLines = Math.min(lines.size(), Math.max(1, (screenHeight - 108) / LINE_HEIGHT));
        height = 44 + visibleLines * LINE_HEIGHT;
        x = (screenWidth - width) / 2;
        y = buttonsAtBottom ? Math.max(4, screenHeight - 57 - height) : 53;
    }

    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    void scroll(double delta) {
        scrollOffset = Math.max(0, Math.min(lines.size() - visibleLines, scrollOffset - (int) Math.signum(delta)));
    }

    void render(GuiGraphics graphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND, x, y, width, height);
        graphics.drawString(
                McUtils.mc().font,
                Component.translatable("screens.wynntils.overlayManagement.help"),
                x + 8,
                y + 8,
                TEXT_COLOR,
                false);
        for (int i = 0; i < visibleLines; i++) {
            graphics.drawString(
                    McUtils.mc().font, lines.get(scrollOffset + i), x + 8, y + 24 + i * LINE_HEIGHT, TEXT_COLOR, false);
        }
        graphics.drawString(
                McUtils.mc().font,
                Component.translatable("screens.wynntils.overlayManagement.help.closeHint"),
                x + 8,
                y + height - 13,
                TEXT_COLOR,
                false);
        if (lines.size() > visibleLines) {
            int trackHeight = visibleLines * LINE_HEIGHT;
            int thumbHeight = Math.max(6, trackHeight * visibleLines / lines.size());
            int thumbY = y + 24 + (trackHeight - thumbHeight) * scrollOffset / (lines.size() - visibleLines);
            graphics.fill(x + width - 12, y + 24, x + width - 10, y + 24 + trackHeight, 0x40604020);
            graphics.fill(x + width - 12, thumbY, x + width - 10, thumbY + thumbHeight, TEXT_COLOR);
        }
    }
}

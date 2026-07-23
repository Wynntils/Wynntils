/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public interface IconRenderer {
    Identifier BANNER_TEXTURE = Identifier.withDefaultNamespace("textures/font/tooltip/banner.png");
    int ATLAS_WIDTH = 48;
    int ATLAS_HEIGHT = 132;
    int ICON_WIDTH = 24;
    int ICON_HEIGHT = 12;
    int SPACING = 12;
    int STAGGER = 6;

    default void renderSkillIcons(GuiGraphics guiGraphics, float baseX, float baseY, int[] elements) {
        if (elements.length == 0) return;

        int min = Math.min(elements.length, 3);
        float offset = 24 - 6 * min - 8;

        for (int i = 0; i < elements.length; i++) {
            float x = baseX + 2 + offset + (i % 3) * SPACING + (i >= 3 ? STAGGER : 0);
            float y = baseY + (elements.length > 3 ? (i >= 3 ? STAGGER : -STAGGER) : 0);
            renderSkillIcon(guiGraphics, x, y, elements[i]);
        }
    }

    default void renderSkillIcon(GuiGraphics guiGraphics, float x, float y, int index) {
        RenderUtils.drawTexturedRect(
                guiGraphics,
                BANNER_TEXTURE,
                x,
                y,
                ICON_WIDTH,
                ICON_HEIGHT,
                ICON_WIDTH,
                (6 + index) * ICON_HEIGHT,
                ICON_WIDTH,
                ICON_HEIGHT,
                ATLAS_WIDTH,
                ATLAS_HEIGHT);
    }

    default void renderAspect(
            GuiGraphics guiGraphics, Texture aspectTexture, Texture aspectFlameTexture, float x, float y) {
        RenderUtils.drawSprite(
                guiGraphics,
                aspectFlameTexture,
                x - (12 * (3f / 4f)) + 5,
                y - 1 + 5,
                aspectFlameTexture.width() * (3f / 4f),
                aspectFlameTexture.height() * (3f / 4f));

        RenderUtils.drawSprite(guiGraphics, aspectTexture, x - 16 + 6, y - 16 + 12);
    }
}

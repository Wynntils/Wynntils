/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;

public class WynntilsColorSwatchFont extends RegisteredFont {
    public WynntilsColorSwatchFont() {
        super("wynntils_color_swatch");
    }

    public static void render(GuiGraphics graphics, float x, float y, float width, float height, CustomColor color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width / 12f, height / 12f);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(WynnFont.asFont("swatch", WynntilsColorSwatchFont.class)
                                .withColor(color.asInt())),
                        0,
                        0,
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        graphics.pose().popMatrix();
    }
}

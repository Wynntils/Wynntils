/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class GuideActionButton extends BasicTexturedButton {
    public GuideActionButton(
            int x, int y, int width, int height, Component message, Consumer<Integer> onClick, Component tooltip) {
        super(x, y, width, height, Texture.GUIDE_WIDGET_BACKGROUND, onClick, List.of());

        setMessage(message);
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                isHovered ? Texture.GUIDE_WIDGET_BACKGROUND_HOVERED : Texture.GUIDE_WIDGET_BACKGROUND,
                getX(),
                getY(),
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(message),
                        getX() + 2,
                        getX() + getWidth() - 2,
                        getY(),
                        getY() + getHeight(),
                        getWidth() - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public void setMessage(Component message) {
        MutableComponent component =
                Component.empty().withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));
        component.append(message);

        super.setMessage(component);
    }
}

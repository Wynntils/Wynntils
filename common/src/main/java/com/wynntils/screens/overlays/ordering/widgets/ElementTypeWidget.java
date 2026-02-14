/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.ordering.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.RenderElementType;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class ElementTypeWidget extends AbstractWidget {
    private static final List<Component> TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlayOrdering.elementWidget.tooltip"));
    private static final Style STYLE = Style.EMPTY
            .withUnderlined(true)
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft")));

    private final RenderElementType elementType;
    private final StyledText text;

    public ElementTypeWidget(int x, int y, RenderElementType elementType) {
        super(x, y, 198, 20, Component.literal(elementType.name()));

        this.elementType = elementType;

        text = StyledText.fromComponent(
                Component.literal(EnumUtils.toNiceString(elementType)).withStyle(STYLE));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.WOOD_STRIP, getX(), getY());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        text,
                        getX(),
                        getX() + width,
                        getY(),
                        getY() + height,
                        width,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (this.isHovered) {
            guiGraphics.setTooltipForNextFrame(
                    Lists.transform(ComponentUtils.wrapTooltips(TOOLTIP, 250), Component::getVisualOrderText),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

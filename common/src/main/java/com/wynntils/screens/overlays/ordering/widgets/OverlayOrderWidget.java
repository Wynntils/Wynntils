/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.ordering.widgets;

import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.overlays.ordering.OverlayOrderingScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class OverlayOrderWidget extends AbstractWidget {
    private final Overlay overlay;
    private final OverlayOrderingScreen orderingScreen;
    private final Button upButton;
    private final Button downButton;

    public OverlayOrderWidget(int x, int y, Overlay overlay, OverlayOrderingScreen orderingScreen) {
        super(x, y, 200, 20, Component.literal(overlay.getTranslatedName()));

        this.overlay = overlay;
        this.orderingScreen = orderingScreen;

        upButton = new Button.Builder(Component.literal("🠝"), (button) -> orderingScreen.reorderOverlay(overlay, -1))
                .pos(x + width - 40, y)
                .size(20, 20)
                .build();

        downButton = new Button.Builder(Component.literal("🠟"), (button) -> orderingScreen.reorderOverlay(overlay, 1))
                .pos(x + width - 20, y)
                .size(20, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.AQUA.withAlpha(isHovered ? 200 : 255),
                getX(),
                getY(),
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(overlay.getTranslatedName()),
                        getX(),
                        getX() + width - 40,
                        getY(),
                        getY() + height,
                        width - 40,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        upButton.render(guiGraphics, mouseX, mouseY, partialTick);
        downButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        upButton.setY(y);
        downButton.setY(y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (upButton.isMouseOver(event.x(), event.y())) {
            return upButton.mouseClicked(event, isDoubleClick);
        } else if (downButton.isMouseOver(event.x(), event.y())) {
            return downButton.mouseClicked(event, isDoubleClick);
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.ordering.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.overlays.ordering.OverlayOrderingScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class OverlayOrderWidget extends AbstractWidget {
    private static final List<Component> TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlayOrdering.overlayWidget.tooltip"));
    private static final List<Component> UP_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlayOrdering.overlayWidget.upTooltip"));
    private static final List<Component> DOWN_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlayOrdering.overlayWidget.downTooltip"));

    private final Overlay overlay;
    private final OverlayOrderingScreen orderingScreen;
    private final Button upButton;
    private final Button downButton;
    private final String textToRender;
    private Style textStyle = Style.EMPTY;

    public OverlayOrderWidget(int x, int y, Overlay overlay, OverlayOrderingScreen orderingScreen) {
        super(x, y, 198, 20, Component.literal(overlay.getTranslatedName()));

        this.overlay = overlay;
        this.orderingScreen = orderingScreen;

        if (overlay instanceof CustomNameProperty customNameProperty) {
            if (!customNameProperty.getCustomName().get().isEmpty()) {
                textToRender = customNameProperty.getCustomName().get();
            } else {
                textToRender = overlay.getTranslatedName();
            }
        } else {
            textToRender = overlay.getTranslatedName();
        }

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
        RenderUtils.drawTexturedRect(guiGraphics, Texture.WIDGET_BACKGROUND_LONG, getX(), getY());

        textStyle = textStyle.withBold(isHovered);

        FontRenderer.getInstance()
                .renderScrollingAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(textToRender).withStyle(textStyle)),
                        getX(),
                        getX() + width - 40,
                        getY(),
                        getY() + height,
                        width - 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        upButton.render(guiGraphics, mouseX, mouseY, partialTick);
        downButton.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.isHovered) {
            List<Component> tooltipToRender = TOOLTIP;

            if (upButton.isHovered()) {
                tooltipToRender = UP_TOOLTIP;
            } else if (downButton.isHovered()) {
                tooltipToRender = DOWN_TOOLTIP;
            }

            guiGraphics.setTooltipForNextFrame(
                    Lists.transform(ComponentUtils.wrapTooltips(tooltipToRender, 250), Component::getVisualOrderText),
                    mouseX,
                    mouseY);
        }
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

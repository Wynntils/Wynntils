/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.secrets.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.MaskedTextInputWidget;
import com.wynntils.services.secrets.type.WynntilsSecret;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SecretInputWidget extends AbstractWidget {
    private final MaskedTextInputWidget maskedTextInputWidget;
    private final Button openLinkButton;
    private final WynntilsSecret wynntilsSecret;

    public SecretInputWidget(
            int x, int y, int width, int height, TextboxScreen textboxScreen, WynntilsSecret wynntilsSecret) {
        super(x, y, width, height, null);

        this.maskedTextInputWidget = new MaskedTextInputWidget(
                x + 120,
                y,
                width - 140,
                height,
                (s) -> Services.Secrets.setSecret(wynntilsSecret, s),
                textboxScreen,
                Services.Secrets.getSecret(wynntilsSecret));
        this.openLinkButton = new Button.Builder(Component.literal("🌐"), (b) -> {
                    Managers.Net.openLink(wynntilsSecret.getUrl());
                })
                .size(20, 20)
                .pos(x + width - 20, y)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.secrets.openLink")))
                .build();
        this.wynntilsSecret = wynntilsSecret;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(EnumUtils.toNiceString(wynntilsSecret)),
                        getX(),
                        getY() + getHeight() / 2f,
                        120,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        maskedTextInputWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        openLinkButton.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered && !openLinkButton.isHovered()) {
            McUtils.screen().setTooltipForNextRenderPass(Component.translatable(wynntilsSecret.getDescriptionKey()));
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        maskedTextInputWidget.setY(y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (maskedTextInputWidget.isMouseOver(event.x(), event.y())) {
            return maskedTextInputWidget.mouseClicked(event, isDoubleClick);
        } else if (openLinkButton.isMouseOver(event.x(), event.y())) {
            return openLinkButton.mouseClicked(event, isDoubleClick);
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (maskedTextInputWidget.isMouseOver(event.x(), event.y())) {
            return maskedTextInputWidget.mouseReleased(event);
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (maskedTextInputWidget.isMouseOver(event.x(), event.y())) {
            return maskedTextInputWidget.mouseDragged(event, dragX, dragY);
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

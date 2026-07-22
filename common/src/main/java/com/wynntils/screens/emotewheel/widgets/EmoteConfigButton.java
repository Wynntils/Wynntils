/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.emotewheel.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.emotewheel.EmoteWheelConfigScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class EmoteConfigButton extends WynntilsButton {
    private static final CustomColor ENABLED_COLOR = new CustomColor(130, 101, 76, 255);
    private static final CustomColor ENABLED_COLOR_BORDER = new CustomColor(94, 72, 55, 255);
    private static final CustomColor ENABLED_COLOR_BORDER_HOVERED = new CustomColor(225, 213, 202, 255);

    private static final CustomColor DISABLED_COLOR = new CustomColor(80, 53, 45, 255);
    private static final CustomColor DISABLED_COLOR_BORDER = new CustomColor(54, 33, 30, 255);
    private static final CustomColor DISABLED_COLOR_BORDER_HOVERED = new CustomColor(225, 213, 202, 255);

    private final String emote;
    private final EmoteWheelConfigScreen selectionScreen;

    public EmoteConfigButton(
            int x, int y, int width, int height, String emote, EmoteWheelConfigScreen selectionScreen) {
        super(x, y, width, height, Component.literal(emote));

        this.emote = emote;
        this.selectionScreen = selectionScreen;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(guiGraphics, getRectColor(), getX(), getY(), width, height);

        RenderUtils.drawRectBorders(guiGraphics, getBorderColor(), getX(), getY(), getX() + width, getY() + height, 2);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(emote),
                        getX() + 4,
                        getY() + (height / 2f),
                        width - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        if (Models.Emote.isFavorited(emote)) {
            int index = Models.Emote.getEmoteIndex(emote);
            String emoteNumber = index == 9 ? "0" : Integer.toString(index + 1);
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(emoteNumber),
                            (getX() + width + 1)
                                    - FontRenderer.getInstance().getFont().width(emoteNumber),
                            getY() + (height / 2f),
                            CommonColors.WHITE,
                            HorizontalAlignment.RIGHT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1.0f);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Prevent interaction when the button is outside of the the scissor area its in
        if ((event.y() <= selectionScreen.getScissorTopY() || event.y() >= selectionScreen.getScissorBottomY())) {
            return false;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Models.Emote.toggleFavorite(emote);
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        // Prevent interaction when the button is outside of the the scissor area its in
        if ((event.y() <= selectionScreen.getScissorTopY() || event.y() >= selectionScreen.getScissorBottomY())) {
            return false;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    private CustomColor getRectColor() {
        return isSelected() ? ENABLED_COLOR : DISABLED_COLOR;
    }

    private CustomColor getBorderColor() {
        if (isSelected()) {
            return isHovered ? ENABLED_COLOR_BORDER_HOVERED : ENABLED_COLOR_BORDER;
        }

        return isHovered ? DISABLED_COLOR_BORDER_HOVERED : DISABLED_COLOR_BORDER;
    }

    private boolean isSelected() {
        return Models.Emote.isFavorited(emote);
    }
}

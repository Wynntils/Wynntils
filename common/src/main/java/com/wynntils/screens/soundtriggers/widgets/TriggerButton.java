/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.soundtriggers.SoundTriggerManagmentScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.soundtriggers.SoundTrigger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TriggerButton extends WynntilsButton {
    private final SoundTrigger trigger;
    private final SoundTriggerManagmentScreen parentScreen;

    private String textToRender;
    private TextInputBoxWidget editInput;

    public TriggerButton(
            int x, int y, int width, int height, SoundTrigger trigger, SoundTriggerManagmentScreen managmentScreen) {
        super(x, y, width, height, Component.literal("Trigger"));

        this.trigger = trigger;
        this.parentScreen = managmentScreen;
        this.textToRender = trigger.getName();
        this.editInput = new TextInputBoxWidget(
                x,
                y,
                width,
                height,
                s -> {
                    trigger.setName(s);
                    parentScreen.soundTriggers.touched();
                },
                parentScreen);
        editInput.visible = false;
        editInput.setTextBoxInput(trigger.getName());
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        CustomColor stateColor = State.getTriggerState(trigger).color;

        RenderUtils.drawRect(guiGraphics, stateColor.withAlpha(100), getX(), getY(), width, height);

        if (parentScreen.getSelectedTrigger() == trigger) {
            stateColor = CommonColors.WHITE;
        } else if (isHovered) {
            stateColor = stateColor.saturationOverwrite(0.075f);
        } else {
            stateColor = stateColor.brightnessOverwrite(0.45f).hueShift(-0.05f);
        }
        RenderUtils.drawRectBorders(guiGraphics, stateColor, getX(), getY(), getX() + width, getY() + height, 2);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(textToRender),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1f);

        editInput.render(guiGraphics, i, j, f);
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Prevent interaction when the tile is outside of the mask from the screen, same applies and released
        if ((event.y() <= parentScreen.getConfigMaskTopY() || event.y() >= parentScreen.getConfigMaskBottomY())) {
            return false;
        }

        if (editInput.visible && editInput.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (parentScreen.getSelectedTrigger() == trigger) {
                editInput.visible = true;
                parentScreen.setFocusedTextInput(editInput);
            } else {
                parentScreen.setSelectedTrigger(trigger);
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        // Prevent interaction when the tile is outside of the mask from the screen, same applies and released
        if ((event.y() <= parentScreen.getConfigMaskTopY() || event.y() >= parentScreen.getConfigMaskBottomY())) {
            return false;
        }

        if (editInput != null) {
            editInput.mouseReleased(event);
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editInput.visible
                && (event.key() == GLFW.GLFW_KEY_ESCAPE
                        || event.key() == GLFW.GLFW_KEY_ENTER
                        || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            editInput.visible = false;
            parentScreen.setFocusedTextInput(null);
        }
        return super.keyPressed(event);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        editInput.setY(y);
    }

    public void hideEditInput() {
        editInput.visible = false;
        parentScreen.setFocusedTextInput(null);
    }

    public SoundTrigger getTrigger() {
        return trigger;
    }

    public enum State {
        ENABLED(CommonColors.GREEN),
        DISABLED(CommonColors.RED),
        ERROR(CommonColors.YELLOW);

        public final CustomColor color;

        State(CustomColor color) {
            this.color = color;
        }

        static State getTriggerState(SoundTrigger trigger) {
            if (!trigger.isEnabled()) return DISABLED;
            if (trigger.getControllerFunctionResult().hasError()
                    || trigger.getIdentifierFunctionResult().hasError()) return ERROR;
            return ENABLED;
        }
    }
}

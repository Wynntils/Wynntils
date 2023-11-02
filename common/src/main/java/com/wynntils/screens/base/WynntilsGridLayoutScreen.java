/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class WynntilsGridLayoutScreen extends WynntilsScreen {
    private static final float GRID_DIVISIONS = 64f;
    // Certain elements require static 20 height or button textures will break
    protected static final int BUTTON_HEIGHT = 20;
    protected float dividedHeight;
    protected float dividedWidth;
    /*
    Some notes on element alignment and sizing:
    All elements should be positioned dynamically relative to window size. That is; you must use this.height and
    this.width, plus any multiplier and optionally, +/- 1 to make small tweaks. For example, these are valid:
    (int) (dividedWidth * 36)
    (int) (dividedWidth * 57) - 1
    (this.width / 2)
    (end of another element) + x
    And these are invalid:
    100
    (int) (dividedWidth * 36) + 4
    (int) (this.width / 2 + 199)
    The reason for all of this is gui scale. Height and width will be different depending on the gui scale, and
    the above method will ensure that the gui is always aligned somewhat correctly.

    Also note that button height (and related elements), must be 20 or the texture will break.
    Element sizing isn't nearly as picky as alignment, but it'll still be better to keep static modifiers (eg. +2) to a
    minimum.
     */

    protected WynntilsGridLayoutScreen(Component component) {
        super(component);
    }

    @Override
    protected void doInit() {
        dividedHeight = this.height / GRID_DIVISIONS;
        dividedWidth = this.width / GRID_DIVISIONS;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (getFocusedTextInput() != null && getFocusedTextInput().charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // When tab is pressed, focus the next text box
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            int index = getFocusedTextInput() == null ? 0 : children().indexOf(getFocusedTextInput());
            int actualIndex = Math.max(index, 0) + 1;

            // Try to find next text input
            // From index - end
            for (int i = actualIndex; i < children().size(); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }

            // From 0 - index
            for (int i = 0; i < Math.min(actualIndex, children().size()); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }
        }

        return (getFocusedTextInput() != null && getFocusedTextInput().keyPressed(keyCode, scanCode, modifiers))
                || super.keyPressed(keyCode, scanCode, modifiers);
    }
}

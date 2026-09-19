/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.screens.base.widgets.TextInputBoxWidget;

public interface TextboxScreen {
    TextInputBoxWidget getFocusedTextInput();

    void setFocusedTextInput(TextInputBoxWidget focusedTextInput);

    static void updateFocus(TextInputBoxWidget oldInput, TextInputBoxWidget newInput) {
        if (oldInput == newInput) return;

        if (oldInput != null) {
            oldInput.setFocused(false);
        }

        if (newInput != null) {
            newInput.setFocused(true);
        }
    }
}

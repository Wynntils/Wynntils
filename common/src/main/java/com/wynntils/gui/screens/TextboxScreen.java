/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.wynntils.gui.widgets.TextInputBoxWidget;

public interface TextboxScreen {
    TextInputBoxWidget getFocusedTextInput();

    void setFocusedTextInput(TextInputBoxWidget focusedTextInput);
}

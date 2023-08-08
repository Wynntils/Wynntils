/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.screens.base.widgets.TextInputBoxWidget;

public interface TextboxScreen {
    TextInputBoxWidget getFocusedTextInput();

    void setFocusedTextInput(TextInputBoxWidget focusedTextInput);
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.wynntils.screens.widgets.TextInputBoxWidget;

public interface SearchableScreen {
    TextInputBoxWidget getFocusedTextInput();

    void setFocusedTextInput(TextInputBoxWidget focusedTextInput);
}

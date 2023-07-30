/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.screens.base.TextboxScreen;
import java.util.function.Consumer;

public class BasicSearchWidget extends SearchWidget<String> {
    public BasicSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    @Override
    public String getSearchQuery() {
        return getTextBoxInput();
    }
}

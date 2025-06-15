/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import java.util.function.Consumer;

public class ContentBookSearchWidget extends SearchWidget {
    public ContentBookSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    // TODO: Custom texture background
}

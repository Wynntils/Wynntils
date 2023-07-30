/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.screens.activities.widgets.QuestBookSearchWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public abstract class BasicWynntilsListScreen<E, B extends WynntilsButton> extends WynntilsListScreen<E, B, String> {
    protected BasicWynntilsListScreen(Component component) {
        // Do not lose search info on re-init
        super(component);

        this.searchWidget = new QuestBookSearchWidget(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15),
                0,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height(),
                s -> reloadElements(),
                this);
    }
}

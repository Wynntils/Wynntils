/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.itemfilter.SearchQuery;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public abstract class WynntilsGuideScreen<E, B extends WynntilsButton> extends WynntilsListScreen<E, B> {
    protected WynntilsGuideScreen(Component component) {
        super(component);
        this.searchWidget =
                new ItemSearchWidget(0, -22, Texture.CONTENT_BOOK_BACKGROUND.width(), 20, q -> reloadElements(), this);
    }

    @Override
    protected void doInit() {
        super.doInit();

        // TODO Add the explanation text

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));
    }

    @Override
    protected final void reloadElementsList(String ignored) {
        /*
        At the time this code is written, this method will always be called with this.searchWidget().getTextInput() as
        argument. Therefore it makes sense to use this hack to migrate from simple text based search to advanced filters
        search.
        This hack is preferable to adding even more complexity in the superclass.
        See https://github.com/Wynntils/Artemis/pull/1860#discussion_r1279180536 for more details.
        */
        if (searchWidget instanceof ItemSearchWidget) { // Should always be true
            reloadElementsList(((ItemSearchWidget) searchWidget).getSearchQuery());
        }
    }

    protected abstract void reloadElementsList(SearchQuery searchQuery);
}

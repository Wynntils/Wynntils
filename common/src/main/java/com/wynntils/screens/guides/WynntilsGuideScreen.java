/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.ItemFilterUIButton;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.network.chat.Component;

public abstract class WynntilsGuideScreen<E, B extends WynntilsButton> extends WynntilsListScreen<E, B> {
    private List<ItemProviderType> supportedProviderTypes;

    protected WynntilsGuideScreen(Component component, List<ItemProviderType> supportedProviderTypes) {
        super(component);

        this.supportedProviderTypes = supportedProviderTypes;

        // Override the search widget with our own
        this.searchWidget = new ItemSearchWidget(
                0,
                -22,
                Texture.CONTENT_BOOK_BACKGROUND.width() - 24,
                20,
                supportedProviderTypes,
                true,
                q -> reloadElements(),
                this);
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new ItemFilterUIButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 20, -22, searchWidget, this, true, supportedProviderTypes));

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
        // We override this method to so we can use our special ItemSearchWidget

        if (!(searchWidget instanceof ItemSearchWidget itemSearchWidget)) {
            WynntilsMod.error(
                    "WynntilsGuideScreen#reloadElementsList was called with a search widget that is not an ItemSearchWidget");
            return;
        }

        reloadElementsList(itemSearchWidget.getSearchQuery());
    }

    protected abstract void reloadElementsList(ItemSearchQuery searchQuery);
}

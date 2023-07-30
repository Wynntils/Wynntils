/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Services;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.TextWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.itemfilter.SearchQuery;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;

public abstract class WynnItemGuideScreen<E, B extends WynntilsButton> extends WynntilsListScreen<E, B, SearchQuery>
        implements TextboxScreen {
    protected WynnItemGuideScreen(Component component) {
        super(component);
        this.searchWidget =
                new ItemSearchWidget(0, -22, Texture.QUEST_BOOK_BACKGROUND.width(), 20, q -> reloadElements(), this);
    }

    @Override
    protected void doInit() {
        super.doInit();

        //TODO Add the explanation text

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));
    }
}

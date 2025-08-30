/*
 * Copyright © Wynntils 2023-2025.
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
import com.wynntils.screens.guides.widgets.filters.FavoriteFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.sorts.GuideSortButton;
import com.wynntils.screens.guides.widgets.sorts.GuideSortWidget;
import com.wynntils.services.itemfilter.statproviders.LevelStatProvider;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

public abstract class WynntilsGuideScreen<E, B extends WynntilsButton> extends WynntilsListScreen<E, B> {
    protected final List<GuideFilterWidget> guideFilterWidgets = new ArrayList<>();
    private final List<ItemProviderType> supportedProviderTypes;

    protected GuideSortWidget guideSortWidget;

    protected WynntilsGuideScreen(Component component, List<ItemProviderType> supportedProviderTypes) {
        super(component);

        this.supportedProviderTypes = supportedProviderTypes;

        originalSearchWidgetX = 0;
        originalSearchWidgetY = -22;

        // Override the search widget with our own
        this.searchWidget = new ItemSearchWidget(
                originalSearchWidgetX,
                originalSearchWidgetY,
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
                Texture.CONTENT_BOOK_BACKGROUND.width() - 20 + offsetX,
                -22 + offsetY,
                searchWidget,
                this,
                true,
                supportedProviderTypes));

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f) + offsetX,
                65 + offsetY,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f
                        + 50
                        - Texture.FORWARD_ARROW_OFFSET.width() / 2f
                        + offsetX),
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50 + offsetX,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));

        addDefaultWidgets();
    }

    public void updateSearchFromQuickFilters() {
        String filters = guideFilterWidgets.stream()
                .map(GuideFilterWidget::getItemSearchQuery)
                .collect(Collectors.joining(" "))
                .trim();
        String sorts = guideSortWidget.getSortQuery().trim();
        String plainQuery = "";

        if (searchWidget != null && searchWidget instanceof ItemSearchWidget itemSearchWidget) {
            plainQuery = String.join(" ", itemSearchWidget.getSearchQuery().plainTextTokens())
                    .trim();
        }

        searchWidget.setTextBoxInput(
                (plainQuery + " " + filters + " " + sorts).trim().replace("  ", " "));
    }

    protected void addDefaultWidgets() {
        if (searchWidget instanceof ItemSearchWidget itemSearchWidget) {
            guideFilterWidgets.clear();

            guideFilterWidgets.add(this.addRenderableWidget(new FavoriteFilterWidget(
                    (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 24) + offsetX,
                    61 + offsetY,
                    this,
                    itemSearchWidget.getSearchQuery())));

            guideSortWidget = this.addRenderableWidget(new GuideSortWidget(13 + offsetX, 171 + offsetY));

            guideSortWidget.setPrimarySortButton(
                    new GuideSortButton(itemSearchWidget.getSearchQuery(), this, LevelStatProvider.class));
        } else {
            WynntilsMod.error("WynntilsGuideScreen's SearchWidget is not an ItemSearchWidget");
        }
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

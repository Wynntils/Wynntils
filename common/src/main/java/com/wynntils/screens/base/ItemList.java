/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.screens.base.widgets.ItemListWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ItemList {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Screen screen;
    private List<? extends ItemListWidget.ListItem> items;
    private final SearchWidget searchWidget;
    private final ItemListWidget itemListWidget;

    public ItemList(
            int x,
            int y,
            int width,
            int height,
            Screen screen,
            List<? extends ItemListWidget.ListItem> items,
            ItemList previous) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.screen = screen;
        this.items = items;

        searchWidget = new SearchWidget(
                x + (width / 2) - 50, y + height - 10 - 20, 100, 20, this::update, ((TextboxScreen) screen));
        screen.addRenderableWidget(searchWidget);
        itemListWidget = new ItemListWidget(x + 5, y + 5, width - 10, height - 10 - 20 - 5 - 5, items);
        screen.addRenderableWidget(itemListWidget);
        if (previous != null) {
            searchWidget.setTextBoxInput(previous.searchWidget.getTextBoxInput());
            itemListWidget.setPage(previous.itemListWidget.getPage());
        }
    }

    public void update(List<? extends ItemListWidget.ListItem> items) {
        this.items = items;
        update(searchWidget.getTextBoxInput());
    }

    public void update(String s) {
        boolean ignoreCase = s.toLowerCase(Locale.ROOT).equals(s);
        itemListWidget.setItems(items.stream()
                .filter(listItem -> {
                    String tooltip = listItem.getTooltip().stream()
                            .map(Component::getString)
                            .collect(Collectors.joining("\n"));
                    if (ignoreCase) {
                        tooltip = tooltip.toLowerCase(Locale.ROOT);
                    }
                    return tooltip.contains(s);
                })
                .toList());
    }
}

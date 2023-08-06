package com.wynntils.screens.base;

import com.wynntils.screens.base.widgets.ItemListWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemList {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Screen screen;
    private final SearchWidget searchWidget;
    private final ItemListWidget itemListWidget;

    public ItemList(int x, int y, int width, int height, Screen screen, List<ItemStack> items) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.screen = screen;

        searchWidget = new SearchWidget(x + (width / 2) - 100, y + height - 10 - 17, 100, 17, this::update, ((TextboxScreen) screen));
        screen.addRenderableWidget(searchWidget);
        itemListWidget = new ItemListWidget(x, y, width, height - 10 - 17 - 5, items);
        screen.addRenderableWidget(itemListWidget);
    }

    public void update(List<ItemStack> items) {
       itemListWidget.setItems(items);
    }

    public void update(String s) {
        itemListWidget.setItems(itemListWidget.getItems().stream().filter(itemStack -> itemStack.getDisplayName().getString().matches(s)).toList());
    }
}

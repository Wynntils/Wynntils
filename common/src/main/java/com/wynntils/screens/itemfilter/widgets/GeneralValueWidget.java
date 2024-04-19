/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GeneralValueWidget extends AbstractWidget {
    protected static final int SCROLLBAR_HEIGHT = 20;
    protected static final int SCROLLBAR_WIDTH = 6;
    protected static final int SCROLLBAR_RENDER_X = 188;

    protected final ItemStatProvider<?> itemStatProvider;
    protected final ItemFilterScreen filterScreen;

    protected List<AbstractWidget> widgets = new ArrayList<>();

    protected GeneralValueWidget(Component title, ItemStatProvider<?> itemStatProvider, ItemFilterScreen filterScreen) {
        super(150, 30, 195, 145, title);

        this.filterScreen = filterScreen;
        this.itemStatProvider = itemStatProvider;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (AbstractWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : widgets) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : widgets) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseReleased(mouseX, mouseY, button);
            }
        }

        return false;
    }

    public abstract void onFiltersChanged(List<StatProviderAndFilterPair> filters);

    protected abstract List<StatProviderAndFilterPair> getFilterPairs();

    protected final void updateQuery() {
        List<StatProviderAndFilterPair> filterPairs = getFilterPairs();
        filterScreen.setFiltersForProvider(itemStatProvider, filterPairs);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

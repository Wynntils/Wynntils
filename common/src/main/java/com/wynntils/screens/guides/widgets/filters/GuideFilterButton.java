/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class GuideFilterButton<T extends ItemStatProvider<?>> extends AbstractWidget {
    protected final Texture texture;

    protected boolean state;

    protected GuideFilterButton(int x, int y, int width, int height, Texture texture) {
        super(x, y, width, height, Component.empty());

        this.texture = texture;
    }

    protected GuideFilterButton(int x, int y, Texture texture) {
        this(x, y, 16, 16, texture);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        state = !state;

        return super.mouseClicked(event, isDoubleClick);
    }

    protected abstract void updateStateFromQuery(ItemSearchQuery searchQuery);

    protected abstract StatProviderAndFilterPair getFilterPair(T provider);

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

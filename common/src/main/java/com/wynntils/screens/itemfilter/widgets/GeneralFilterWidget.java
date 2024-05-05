/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GeneralFilterWidget extends AbstractWidget {
    protected final ProviderFilterListWidget parent;

    protected GeneralFilterWidget(
            int x, int y, int width, int height, Component title, ProviderFilterListWidget parent) {
        super(x, y, width, height, title);

        this.parent = parent;
    }

    public abstract void updateY(int y);

    protected abstract StatProviderAndFilterPair getFilterPair();

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

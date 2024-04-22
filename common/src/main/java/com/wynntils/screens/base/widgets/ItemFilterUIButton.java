/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ItemFilterUIButton extends WynntilsButton {
    private final SearchWidget searchWidget;
    private final Screen previousScreen;
    private final boolean supportsSorting;
    private final List<ItemProviderType> supportedProviderTypes;

    public ItemFilterUIButton(
            int x,
            int y,
            SearchWidget searchWidget,
            Screen previousScreen,
            boolean supportsSorting,
            List<ItemProviderType> supportedProviderTypes) {
        super(x, y, 20, 20, Component.literal("✎"));
        this.searchWidget = searchWidget;
        this.previousScreen = previousScreen;
        this.supportsSorting = supportsSorting;
        this.supportedProviderTypes = supportedProviderTypes;
        this.setTooltip(Tooltip.create(Component.translatable("screens.wynntils.itemSearchButton.tooltip")));
    }

    @Override
    public void onPress() {
        McUtils.mc()
                .setScreen(
                        ItemFilterScreen.create(searchWidget, previousScreen, supportsSorting, supportedProviderTypes));
    }
}

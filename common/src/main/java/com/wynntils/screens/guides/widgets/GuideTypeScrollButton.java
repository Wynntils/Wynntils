/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.screens.guides.WynntilsGuideScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuideTypeScrollButton extends GuideNavigationButton {
    private final boolean up;
    private final WynntilsGuideScreen guideScreen;

    public GuideTypeScrollButton(int x, int y, ItemStack itemToRender, boolean up, WynntilsGuideScreen guideScreen) {
        super(x, y, itemToRender);
        this.up = up;
        this.guideScreen = guideScreen;
    }

    @Override
    protected Component getTooltip() {
        return Component.literal("Go " + (up ? "up" : "down"));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        guideScreen.scrollTypes(up ? -1 : 1);
    }
}

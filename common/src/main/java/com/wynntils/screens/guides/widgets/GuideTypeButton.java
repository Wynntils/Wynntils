/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.utils.EnumUtils;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuideTypeButton extends GuideNavigationButton {
    private final WynntilsGuideScreen.GuideType guideType;
    private final WynntilsGuideScreen guideScreen;

    public GuideTypeButton(
            int x,
            int y,
            ItemStack itemToRender,
            WynntilsGuideScreen.GuideType guideType,
            WynntilsGuideScreen guideScreen) {
        super(x, y, itemToRender);
        this.guideType = guideType;
        this.guideScreen = guideScreen;
    }

    @Override
    protected Component getTooltip() {
        return Component.literal(EnumUtils.toNiceString(guideType));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        guideScreen.setGuideType(guideType);
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.screens.maps.SeaskipperMapScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomSeaskipperScreenFeature extends Feature {
    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        if (!Models.Container.isSeaskipper(
                ComponentUtils.getUnformatted(event.getScreen().getTitle()))) {
            return;
        }

        McUtils.mc().setScreen(SeaskipperMapScreen.create());
    }
}

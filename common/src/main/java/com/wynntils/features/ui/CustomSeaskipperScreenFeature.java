/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.screens.maps.SeaskipperDepartureBoardScreen;
import com.wynntils.screens.maps.SeaskipperMapScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.PointerType;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomSeaskipperScreenFeature extends Feature {
    @RegisterConfig
    public final Config<SeaskipperScreenType> screenType = new Config<>(SeaskipperScreenType.DEPARTURE_BOARD);

    @RegisterConfig
    public final Config<PointerType> pointerType = new Config<>(PointerType.ARROW);

    @RegisterConfig
    public final Config<CustomColor> pointerColor = new Config<>(new CustomColor(1f, 1f, 1f, 1f));

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        if (!Models.Container.isSeaskipper(event.getScreen().getTitle())) return;

        if (!Models.Seaskipper.isProfileLoaded()) {
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.customSeaskipperScreen.error.noProfile"));
            return;
        }

        if (screenType.get() == SeaskipperScreenType.DEPARTURE_BOARD) {
            McUtils.mc().setScreen(SeaskipperDepartureBoardScreen.create());
        } else if (screenType.get() == SeaskipperScreenType.MAP) {
            McUtils.mc().setScreen(SeaskipperMapScreen.create());
        }
    }

    public enum SeaskipperScreenType {
        DEPARTURE_BOARD,
        MAP
    }
}

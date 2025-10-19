/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ScreenSettingsButton extends GeneralSettingsButton {
    private final Config<?> config;
    private final Screen screen;

    protected ScreenSettingsButton(int x, int y, Config<?> config, Screen screen, int maskTopY, int maskBottomY) {
        super(
                x,
                y,
                90,
                20,
                Component.literal(config.getValueString()),
                ComponentUtils.wrapTooltips(List.of(Component.literal(config.getDescription())), 150),
                maskTopY,
                maskBottomY);
        this.config = config;
        this.screen = screen;
    }

    @Override
    public void onPress() {
        McUtils.setScreen(screen);
    }
}

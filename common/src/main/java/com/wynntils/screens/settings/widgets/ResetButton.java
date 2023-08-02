/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.config.Config;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ResetButton extends GeneralSettingsButton {
    private final Config<?> config;
    private final Runnable onClick;

    ResetButton(Config<?> config, Runnable onClick, int x, int y) {
        super(
                x,
                y,
                35,
                12,
                Component.translatable("screens.wynntils.settingsScreen.reset.name"),
                List.of(Component.translatable("screens.wynntils.settingsScreen.reset.description")));
        this.config = config;
        this.onClick = onClick;
    }

    @Override
    protected CustomColor getTextColor() {
        return config.valueChanged() ? CommonColors.WHITE : CommonColors.GRAY;
    }

    @Override
    protected CustomColor getBackgroundColor() {
        return config.valueChanged() ? super.getBackgroundColor() : BACKGROUND_COLOR;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!config.valueChanged()) return;
        super.playDownSound(handler);
    }

    @Override
    public void onPress() {
        if (!config.valueChanged()) return;
        config.reset();
        onClick.run();
    }
}

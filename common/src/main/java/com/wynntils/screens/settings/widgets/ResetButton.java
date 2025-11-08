/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.List;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ResetButton extends GeneralSettingsButton {
    private final Config<?> config;
    private final Runnable onClick;

    ResetButton(Config<?> config, Runnable onClick, int x, int y, int maskTopY, int maskBottomY) {
        super(
                x,
                y,
                35,
                FontRenderer.getInstance().getFont().lineHeight + 8,
                Component.translatable("screens.wynntils.settingsScreen.reset.name"),
                List.of(Component.translatable("screens.wynntils.settingsScreen.reset.description")),
                maskTopY,
                maskBottomY);
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

        // Reload configurables to update checkbox
        if (McUtils.screen() instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.populateConfigurables();
        }
    }
}

/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.config.ConfigHolder;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BooleanSettingsButton extends GeneralSettingsButton {
    private final ConfigHolder configHolder;

    public BooleanSettingsButton(ConfigHolder configHolder) {
        super(
                0,
                7,
                50,
                FontRenderer.getInstance().getFont().lineHeight + 8,
                getTitle(configHolder),
                List.of(Component.literal(configHolder.getDescription())));
        this.configHolder = configHolder;
    }

    @Override
    public void onPress() {
        configHolder.setValue(!isEnabled(configHolder));
        setMessage(getTitle(configHolder));
    }

    private static MutableComponent getTitle(ConfigHolder configHolder) {
        return isEnabled(configHolder)
                ? Component.translatable("screens.wynntils.settingsScreen.booleanConfig.enabled")
                : Component.translatable("screens.wynntils.settingsScreen.booleanConfig.disabled");
    }

    @Override
    protected CustomColor getTextColor() {
        return isEnabled(configHolder) ? CommonColors.GREEN : CommonColors.RED;
    }

    private static boolean isEnabled(ConfigHolder configHolder) {
        return configHolder.getValue() == Boolean.TRUE;
    }
}

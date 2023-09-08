/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ApplyButton extends GeneralSettingsButton {
    private final WynntilsBookSettingsScreen screen;

    public ApplyButton(WynntilsBookSettingsScreen screen) {
        super(
                55,
                Texture.CONFIG_BOOK_BACKGROUND.height() - 30,
                35,
                14,
                Component.translatable("screens.wynntils.settingsScreen.apply"),
                List.of(Component.translatable("screens.wynntils.settingsScreen.apply.description")
                        .withStyle(ChatFormatting.GREEN)));
        this.screen = screen;
    }

    @Override
    public void onPress() {
        Managers.Config.saveConfig();
        screen.onClose();
    }
}

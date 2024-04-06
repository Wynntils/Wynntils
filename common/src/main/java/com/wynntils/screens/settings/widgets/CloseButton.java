/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CloseButton extends GeneralSettingsButton {
    private final WynntilsBookSettingsScreen screen;

    public CloseButton(WynntilsBookSettingsScreen screen) {
        super(
                15,
                Texture.CONFIG_BOOK_BACKGROUND.height() - 30,
                35,
                14,
                Component.translatable("screens.wynntils.settingsScreen.close"),
                List.of(Component.translatable("screens.wynntils.settingsScreen.close.description")
                        .withStyle(ChatFormatting.DARK_RED)));
        this.screen = screen;
    }

    @Override
    public void onPress() {
        screen.onClose();
    }
}

/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.base.TextboxScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/**
 * Creates an empty Config option to add a custom widget in the settings menu
 * (like a button to open a new screen).
 */
public class CustomConfigScreen {
    // For GSON
    public CustomConfigScreen() {}

    /**
     * This String-based constructor is implicitly called from {@link Config#tryParseStringValue}.
     */
    public CustomConfigScreen(String string) {}

    public AbstractWidget customScreenWidget(
            int renderX, int renderY, Config<?> configOption, Screen screen, int maskTopY, int maskBottomY) {
        return new TextInputBoxSettingsWidget<>(
                renderX, renderY, configOption, (TextboxScreen) screen, maskTopY, maskBottomY);
    }
}

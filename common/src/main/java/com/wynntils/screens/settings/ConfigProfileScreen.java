/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigProfileScreen extends WynntilsScreen {
    private final Screen previousScreen;

    private ConfigProfileScreen(Screen previousScreen) {
        super(Component.translatable("screens.wynntils.configProfilesScreen.name"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new ConfigProfileScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
}

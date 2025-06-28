/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OverlaySettingsWidget extends ScreenSettingsButton {
    public OverlaySettingsWidget(
            int x, int y, Config<?> config, Screen returnScreen, int maskTopY, int maskBottomY, Overlay overlay) {
        super(x, y, config, OverlayManagementScreen.create(returnScreen, overlay), maskTopY, maskBottomY);
    }

    @Override
    public Component getMessage() {
        return Component.translatable("screens.wynntils.overlaySelection.edit");
    }
}

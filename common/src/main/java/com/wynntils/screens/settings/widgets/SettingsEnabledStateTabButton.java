/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class SettingsEnabledStateTabButton extends GeneralSettingsTabButton {
    public SettingsEnabledStateTabButton(
            int x,
            int y,
            int width,
            int height,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            Texture icon,
            int offsetX,
            int offsetY) {
        super(x, y, width, height, onClick, tooltip, Texture.TAG_RED, icon, OffsetDirection.UP, offsetX, offsetY);
    }

    public void updateIcon(Texture icon) {
        this.iconTexture = icon;
    }
}

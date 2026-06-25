/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.screens.Screen;

public interface ExternalConfigurationScreen {
    Screen getExternalConfigurationScreen(Screen previousScreen);

    default Texture getButtonTexture() {
        return Texture.OPEN_EXTERNAL;
    }

    default Texture getHoverTexture() {
        return Texture.OPEN_EXTERNAL_HOVER;
    }

    default String getTranslationKey() {
        return "externalConfigurationScreen";
    }

    default Object[] getTranslationObjects() {
        return new Object[0];
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.config.ConfigHolder;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

public class TextInputBoxSettingsWidget extends TextInputBoxWidget {
    protected final ConfigHolder configHolder;

    protected TextInputBoxSettingsWidget(
            int x, int y, ConfigHolder configHolder, ContainerEventHandler containerStateAccess, int width) {
        super(x, y - 1, width, FontRenderer.getInstance().getFont().lineHeight + 8, null, containerStateAccess);
        this.configHolder = configHolder;
        setTextBoxInput(configHolder.getValue().toString());
    }

    public TextInputBoxSettingsWidget(
            int x, int y, ConfigHolder configHolder, ContainerEventHandler containerStateAccess) {
        this(x, y, configHolder, containerStateAccess, 100);
    }

    @Override
    protected void onUpdate(String text) {
        Object parsedValue = configHolder.tryParseStringValue(text);
        if (parsedValue != null) {
            if (!parsedValue.equals(configHolder.getValue())) {
                configHolder.setValue(parsedValue);
            }

            setRenderColor(CommonColors.GREEN);
        } else {
            setRenderColor(CommonColors.RED);
        }
    }
}

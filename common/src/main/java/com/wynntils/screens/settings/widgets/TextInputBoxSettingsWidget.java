/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.config.ConfigHolder;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;

public class TextInputBoxSettingsWidget extends TextInputBoxWidget {
    protected final ConfigHolder configHolder;

    protected TextInputBoxSettingsWidget(ConfigHolder configHolder, TextboxScreen textboxScreen, int width) {
        super(0, 6, width, FontRenderer.getInstance().getFont().lineHeight + 8, null, textboxScreen);
        this.configHolder = configHolder;
        setTextBoxInput(configHolder.getValue().toString());
    }

    public TextInputBoxSettingsWidget(ConfigHolder configHolder, TextboxScreen textboxScreen) {
        this(configHolder, textboxScreen, 100);
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

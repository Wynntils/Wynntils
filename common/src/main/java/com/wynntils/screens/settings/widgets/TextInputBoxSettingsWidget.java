/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;

public class TextInputBoxSettingsWidget<T> extends TextInputBoxWidget {
    protected final Config<T> config;

    protected TextInputBoxSettingsWidget(Config<T> config, TextboxScreen textboxScreen, int width) {
        super(0, 6, width, FontRenderer.getInstance().getFont().lineHeight + 8, null, textboxScreen);
        this.config = config;
        setTextBoxInput(config.getValue().toString());
    }

    public TextInputBoxSettingsWidget(Config<T> config, TextboxScreen textboxScreen) {
        this(config, textboxScreen, 100);
    }

    @Override
    protected void onUpdate(String text) {
        T parsedValue = config.tryParseStringValue(text);
        if (parsedValue != null) {
            if (!parsedValue.equals(config.getValue())) {
                config.setValue(parsedValue);
            }

            setRenderColor(CommonColors.GREEN);
        } else {
            setRenderColor(CommonColors.RED);
        }
    }
}

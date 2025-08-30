/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextInputBoxSettingsWidget<T> extends TextInputBoxWidget {
    private final Config<T> config;
    private final int maskTopY;
    private final int maskBottomY;

    protected TextInputBoxSettingsWidget(
            int x, int y, Config<T> config, TextboxScreen textboxScreen, int maskTopY, int maskBottomY) {
        super(x, y, 90, 20, null, textboxScreen);
        this.config = config;
        this.maskTopY = maskTopY;
        this.maskBottomY = maskBottomY;
        setTextBoxInput(config.get().toString());
        tooltip = ComponentUtils.wrapTooltips(List.of(Component.literal(config.getDescription())), 150);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Don't want to display tooltip when the tile is outside the mask from the screen
        if (isHovered && (mouseY <= maskTopY || mouseY >= maskBottomY)) {
            isHovered = false;
        }

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void onUpdate(String text) {
        T parsedValue = config.tryParseStringValue(text);
        if (parsedValue != null) {
            if (!parsedValue.equals(config.get())) {
                config.setValue(parsedValue);
            }

            setRenderColor(CommonColors.GREEN);
        } else {
            setRenderColor(CommonColors.RED);
        }
    }
}

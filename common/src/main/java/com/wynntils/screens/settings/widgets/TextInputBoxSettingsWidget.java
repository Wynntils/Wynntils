/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayHistory;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.overlays.selection.OverlaySettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class TextInputBoxSettingsWidget<T> extends TextInputBoxWidget {
    private final Config<T> config;
    private final int maskTopY;
    private final int maskBottomY;

    protected TextInputBoxSettingsWidget(
            int x,
            int y,
            Config<T> config,
            List<Component> tooltip,
            TextboxScreen textboxScreen,
            int maskTopY,
            int maskBottomY) {
        super(x, y, 90, 20, null, textboxScreen);
        this.config = config;
        this.maskTopY = maskTopY;
        this.maskBottomY = maskBottomY;
        setTextBoxInput(config.get().toString());
        this.tooltip = tooltip;
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
    public boolean keyPressed(KeyEvent event) {
        if (textboxScreen instanceof OverlaySettingsScreen settings
                && KeyboardUtils.isControlDown()
                && (event.key() == InputConstants.KEY_Z || event.key() == InputConstants.KEY_Y)) {
            OverlayHistory history = OverlayHistory.isPlacement(config)
                    ? Managers.Overlay.getPlacementHistory()
                    : Managers.Overlay.getSettingsHistory();
            if (history.restoreConfig(config, event.key() == InputConstants.KEY_Y || KeyboardUtils.isShiftDown())) {
                setTextBoxInput(config.get().toString());
                settings.updateHistoryButtons();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void onUpdate(String text) {
        T parsedValue = config.tryParseStringValue(text);
        if (parsedValue != null) {
            if (!parsedValue.equals(config.get())) {
                if (textboxScreen instanceof OverlaySettingsScreen settings
                        && Managers.Persisted.getMetadata(config).owner() instanceof Overlay overlay) {
                    settings.beginSettingEdit(overlay, config);
                }
                config.setValue(parsedValue);
            }

            setRenderColor(CommonColors.GREEN);
        } else {
            setRenderColor(CommonColors.RED);
        }
    }
}

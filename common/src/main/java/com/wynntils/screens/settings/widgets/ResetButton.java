/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ResetButton extends GeneralSettingsButton {
    private final Config<?> config;
    private final Runnable onClick;

    ResetButton(Config<?> config, Runnable onClick, int x, int y, int maskTopY, int maskBottomY) {
        super(
                x,
                y,
                35,
                FontRenderer.getInstance().getFont().lineHeight + 8,
                Component.translatable("screens.wynntils.settingsScreen.reset.name"),
                List.of(Component.translatable("screens.wynntils.settingsScreen.reset.description")),
                maskTopY,
                maskBottomY);
        this.config = config;
        this.onClick = onClick;
    }

    @Override
    protected void handleCursor(GuiGraphics guiGraphics) {
        if (this.isHovered()) {
            guiGraphics.requestCursor(config.valueChanged() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        }
    }

    @Override
    protected CustomColor getTextColor() {
        return config.valueChanged() ? CommonColors.WHITE : CommonColors.GRAY;
    }

    @Override
    protected CustomColor getBackgroundColor() {
        return config.valueChanged() ? super.getBackgroundColor() : BACKGROUND_COLOR;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!config.valueChanged()) return;
        super.playDownSound(handler);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!config.valueChanged()) return;
        config.reset();
        onClick.run();

        // Reload configurables to update checkbox
        if (McUtils.screen() instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.populateConfigurables();
        }
    }
}

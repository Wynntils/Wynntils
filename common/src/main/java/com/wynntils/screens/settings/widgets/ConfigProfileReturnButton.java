/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.wynnfonts.BannerPillFont;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.ConfigProfileScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ConfigProfileReturnButton extends WynntilsButton {
    private final Component returnComponent;
    private final Component returnComponentHovered;
    private final ConfigProfileScreen screen;

    public ConfigProfileReturnButton(int x, int y, int width, int height, ConfigProfileScreen screen) {
        super(x, y, width, height, Component.literal("Config Profile Return Button"));

        this.screen = screen;

        this.returnComponent = BannerPillFont.buildMessage(
                "return", CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY), CommonColors.WHITE, "");
        this.returnComponentHovered = BannerPillFont.buildMessage(
                "return", CustomColor.fromChatFormatting(ChatFormatting.GRAY), CommonColors.WHITE, "");
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(isHovered ? returnComponentHovered : returnComponent),
                        getX() + width / 2f,
                        getY() + height,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.OUTLINE,
                        2f);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        screen.onClose();
    }
}

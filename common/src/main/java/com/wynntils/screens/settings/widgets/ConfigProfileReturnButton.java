/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.ConfigProfileScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class ConfigProfileReturnButton extends WynntilsButton {
    private static final ResourceLocation PILL_FONT = ResourceLocation.withDefaultNamespace("banner/pill");
    private static final MutableComponent RETURN_BACKGROUND = Component.literal(
                    "\uDAFF\uDFFF\uE060\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE044\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE062\uDAFF\uDFDA")
            .withStyle(Style.EMPTY.withFont(PILL_FONT));
    private static final MutableComponent RETURN_FOREGROUND = Component.literal("\uE011\uE004\uE013\uE014\uE011\uE00D")
            .withStyle(Style.EMPTY.withFont(PILL_FONT).withColor(ChatFormatting.WHITE));

    private final ConfigProfileScreen screen;

    public ConfigProfileReturnButton(int x, int y, int width, int height, ConfigProfileScreen screen) {
        super(x, y, width, height, Component.literal("Config Profile Return Button"));

        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(RETURN_BACKGROUND.withStyle(
                                        isHovered ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY))
                                .append(StyledText.fromComponent(RETURN_FOREGROUND)),
                        getX() + width / 2f,
                        getY() + height,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.OUTLINE,
                        2f);
    }

    @Override
    public void onPress() {
        screen.onClose();
    }
}

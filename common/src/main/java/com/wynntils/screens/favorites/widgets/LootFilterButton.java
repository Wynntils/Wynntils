/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.favorites.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class LootFilterButton extends WynntilsButton {
    private static final int TEXT_PADDING = 4;

    private final Texture texture;
    private final Runnable onPress;

    public LootFilterButton(int x, int y, int width, int height, Component message, Texture texture, Runnable onPress) {
        super(x, y, width, height, message);
        this.texture = texture;
        this.onPress = onPress;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                active ? texture : Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                getX(),
                getY(),
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(RenderedStringUtils.getMaxFittingText(
                                getMessage().getString(),
                                this.width - TEXT_PADDING * 2,
                                FontRenderer.getInstance().getFont())),
                        getX() + this.width / 2f,
                        getY() + this.height / 2f,
                        active ? CommonColors.WHITE : CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }
}

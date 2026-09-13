/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderHeaderWidget extends AbstractProviderWidget {
    private static final int PROVIDER_HEIGHT = 16;

    public ProviderHeaderWidget(ProviderType providerType) {
        super(Component.literal(providerType.getDisplayName()), 26, providerType);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.CATEGORY_MANAGER_OPTION_HEADER,
                getX(),
                getY() + (this.height - PROVIDER_HEIGHT) / 2f,
                150,
                PROVIDER_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(getMessage().getString()),
                        getX() + 20,
                        getY() + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.CATEGORY_MANAGER_OPTION_HEADER_LINE,
                getX() + 150 + 5,
                getY() + (this.height - PROVIDER_HEIGHT) / 2f,
                this.width - 150 - 5,
                PROVIDER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

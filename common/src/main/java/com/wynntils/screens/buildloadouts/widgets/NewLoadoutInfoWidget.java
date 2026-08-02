/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class NewLoadoutInfoWidget extends AbstractWidget {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private StyledText text;
    private boolean isInfo = true;

    public NewLoadoutInfoWidget(int x, int y, int width, int height, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal("New Loadout Info Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (text == null || text.isEmpty()) return;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                isInfo ? Texture.BUILD_LOADOUTS_INFO_WIDGET_BOX : Texture.BUILD_LOADOUTS_WARNING_WIDGET_BOX,
                this.x,
                this.y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        this.text,
                        this.x + 10 + Texture.BUILD_LOADOUTS_INFO_ICON.width() + 5,
                        this.y + 10,
                        this.y + this.height - 10,
                        this.width - Texture.BUILD_LOADOUTS_INFO_ICON.width() - 20,
                        isInfo ? CustomColor.fromInt(0x242424) : parent.ERROR_COLOR,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);
        if (isInfo) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_INFO_ICON,
                    x + 10,
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_INFO_ICON.height() / 2f);
        } else {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WARNING_ICON,
                    x + 10,
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_WARNING_ICON.height() / 2f);
        }
    }

    public void setText(StyledText text, boolean isInfo) {
        this.text = text;
        this.isInfo = isInfo;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

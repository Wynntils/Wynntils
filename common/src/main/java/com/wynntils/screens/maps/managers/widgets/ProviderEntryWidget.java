/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderEntryWidget extends AbstractWidget {
    private static final int ENTRY_HEIGHT = 28;
    protected static final int TEXT_PADDING = 5;

    private final String providerId;
    private final Supplier<String> selectedProviderId;
    private final Consumer<String> onSelect;

    public ProviderEntryWidget(
            String providerId, Component name, Supplier<String> selectedProviderId, Consumer<String> onSelect) {
        super(0, 0, 0, ENTRY_HEIGHT, name);
        this.providerId = providerId;
        this.selectedProviderId = selectedProviderId;
        this.onSelect = onSelect;
    }

    public String getProviderId() {
        return providerId;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        boolean selected = providerId.equals(selectedProviderId.get());
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                selected ? Texture.MANAGER_WIDGET_BACKGROUND_RED : Texture.MANAGER_WIDGET_BACKGROUND,
                getX(),
                getY(),
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(getMessage()),
                        getX() + TEXT_PADDING,
                        getY() + getHeight() / 2f,
                        getTextMaxWidth(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        renderExtras(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected int getTextMaxWidth() {
        return getWidth() - TEXT_PADDING * 2;
    }

    protected void renderExtras(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        if (!isMouseOver(event.x(), event.y())) return false;

        playDownSound(McUtils.mc().getSoundManager());
        onSelect.accept(providerId);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

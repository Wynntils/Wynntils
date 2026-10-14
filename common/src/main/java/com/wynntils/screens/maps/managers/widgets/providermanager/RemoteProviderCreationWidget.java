/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RemoteProviderCreationWidget extends AbstractWidget {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BOX_HEIGHT = 20;
    private static final int PADDING = 5;

    private final ProviderManagementScreen parent;

    private final TexturedTextInputBoxWidget nameTextBox;
    private final TexturedTextInputBoxWidget urlTextBox;
    private final SaveRemoteProviderButton saveButton;
    private final List<AbstractWidget> children;

    public RemoteProviderCreationWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height, Component.literal("Remote Provider Creation Widget"));
        this.parent = parent;

        nameTextBox = new TexturedTextInputBoxWidget(
                x + PADDING,
                y + 30,
                width - PADDING * 2,
                BOX_HEIGHT,
                text -> {},
                parent,
                TexturedTextInputBoxWidget.Mode.STRING);

        urlTextBox = new TexturedTextInputBoxWidget(
                x + PADDING,
                y + 70,
                width - PADDING * 2,
                BOX_HEIGHT,
                text -> {},
                parent,
                TexturedTextInputBoxWidget.Mode.STRING);

        saveButton = new SaveRemoteProviderButton(
                x + width / 2 - BUTTON_WIDTH / 2,
                y + 108,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                parent,
                nameTextBox,
                urlTextBox);

        children = List.of(nameTextBox, urlTextBox, saveButton);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) return;

        TextInputBoxWidget focused = parent.getFocusedTextInput();
        if (focused == nameTextBox || focused == urlTextBox) {
            parent.setFocusedTextInput(null);
        }
    }

    public SaveRemoteProviderButton getSaveButton() {
        return saveButton;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, getX(), getY(), getWidth(), getHeight());

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.CATEGORY_MANAGER_OPTION_HEADER, getX() + 10, getY() - 7, 150, 16);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.remoteProviderCreationWidget.remoteProviderText")),
                        getX() + 30,
                        getY() + 1,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.remoteProviderCreationWidget.providerNameText")),
                        getX() + PADDING,
                        getY() + 23,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.remoteProviderCreationWidget.providerUrlText")),
                        getX() + PADDING,
                        getY() + 63,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        children.forEach(c -> c.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!this.visible || !this.active) return false;

        for (AbstractWidget child : children) {
            if (child.isMouseOver(event.x(), event.y()) && child.mouseClicked(event, isDoubleClick)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!this.visible || !this.active) return false;

        boolean handled = false;
        for (AbstractWidget child : children) {
            if (!child.isMouseOver(event.x(), event.y())) continue;

            handled = handled || child.mouseReleased(event);
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!this.visible || !this.active) return false;

        for (AbstractWidget child : children) {
            if (!child.isMouseOver(event.x(), event.y())) continue;

            if (child.mouseDragged(event, dragX, dragY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

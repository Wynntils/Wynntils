/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.IconSelectionScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IconOptionWidget extends AbstractOptionWidget<String> {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ICON_BORDER = 4;

    public IconOptionWidget(
            Component label,
            Component description,
            OptionCategory category,
            Function<MapAttributes, Optional<String>> valueGetter) {
        super(label, description, 32, category, valueGetter);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOverIconButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(getMessage().getString()),
                        getX(),
                        getY() + this.height / 2f,
                        !this.inherited || isOverridden() ? CommonColors.WHITE : CommonColors.GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                getX() + this.width - BUTTON_WIDTH,
                getY() + (this.height - BUTTON_HEIGHT) / 2f,
                this.BUTTON_WIDTH,
                this.BUTTON_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.iconOptionWidget.editIconText")),
                        getX() + this.width - BUTTON_WIDTH / 2f,
                        getY() + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        boolean isNoneIcon = value == null || value.equals(MapIcon.NO_ICON_ID);

        if (isNoneIcon) {
            Font font = FontRenderer.getInstance().getFont();
            Component noneText =
                    Component.translatable("screens.wynntils.map.managers.categoryManager.iconOptionWidget.noneText");
            int textWidth = font.width(noneText);
            int textHeight = font.lineHeight;

            int iconBoxWidth = textWidth + ICON_BORDER * 2;
            int iconBoxHeight = Math.min(textHeight + ICON_BORDER * 2, this.height);

            int iconBoxX = getX() + this.width - BUTTON_WIDTH - iconBoxWidth - 5;
            int iconBoxY = getY() + (this.height - iconBoxHeight) / 2;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxWidth, iconBoxHeight);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(noneText),
                            iconBoxX + iconBoxWidth / 2f,
                            iconBoxY + iconBoxHeight / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            MapIcon icon = Services.MapData.getIconOrFallback(value);
            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            int iconBoxSize = Math.min(Math.max(iconWidth, iconHeight) + ICON_BORDER * 2, this.height);

            int iconBoxX = getX() + this.width - BUTTON_WIDTH - iconBoxSize - 5;
            int iconBoxY = getY() + (this.height - iconBoxSize) / 2;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    RenderPipelines.GUI_TEXTURED,
                    icon.getIdentifier(),
                    CommonColors.WHITE,
                    iconBoxX + (iconBoxSize - iconWidth) / 2f,
                    iconBoxY + (iconBoxSize - iconHeight) / 2f,
                    iconWidth,
                    iconHeight,
                    0f,
                    0f,
                    iconWidth,
                    iconHeight,
                    iconWidth,
                    iconHeight);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseOverIconButton(event.x(), event.y())) {
            this.playDownSound(McUtils.mc().getSoundManager());

            CategoryManagementScreen currentScreen = (CategoryManagementScreen) McUtils.screen();
            McUtils.mc()
                    .setScreen(new IconSelectionScreen(
                            currentScreen,
                            icon -> setValue(icon == null ? MapIcon.NO_ICON_ID : icon.getIconId()),
                            value));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isMouseOverIconButton(double mouseX, double mouseY) {
        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                getX() + getWidth() - BUTTON_WIDTH,
                getX() + getWidth() - 1,
                getY() + (this.height - BUTTON_HEIGHT) / 2,
                getY() + (this.height - BUTTON_HEIGHT) / 2 + BUTTON_HEIGHT);
    }
}

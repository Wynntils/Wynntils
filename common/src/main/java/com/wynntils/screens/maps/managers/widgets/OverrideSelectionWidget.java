/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class OverrideSelectionWidget extends AbstractWidget {
    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 8;
    private static final int ARROW_PADDING = 4;
    private static final int OPTION_HORIZONTAL_PADDING = 4;
    private static final int OPTION_VERTICAL_SPACING = 2;

    private static final OverrideType[] OVERRIDE_TYPES = OverrideType.values();

    private final int x;
    private final int y;
    private final int collapsedHeight;
    private final int optionHeight;
    private final CategoryManagementScreen parent;

    private boolean expanded = false;
    private OverrideType selectedType;

    public OverrideSelectionWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Override Selection Widget"));
        this.x = x;
        this.y = y;
        this.collapsedHeight = height;
        this.optionHeight = height;
        this.parent = parent;
        this.selectedType = parent.getSelectedOverrideType();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOverArrow(mouseX, mouseY) || isMouseOverAnyOption(mouseX, mouseY)) {
            this.handleCursor(guiGraphics);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, x, y, this.width, collapsedHeight);

        renderArrow(guiGraphics);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(selectedType.getDisplayName()),
                        x + ARROW_PADDING * 2 + ARROW_WIDTH,
                        y + collapsedHeight / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (expanded) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.MANAGER_WIDGET_BACKGROUND,
                    x,
                    y + collapsedHeight - 1,
                    this.width,
                    getOptionsAreaHeight());

            renderOptions(guiGraphics);
        }
    }

    private void renderOptions(GuiGraphics guiGraphics) {
        for (int i = 0; i < OVERRIDE_TYPES.length; i++) {
            int rowY = getOptionRowY(i);
            int rowX = x + OPTION_HORIZONTAL_PADDING;
            int rowWidth = this.width - OPTION_HORIZONTAL_PADDING * 2;

            Texture backgroundTexture = (OVERRIDE_TYPES[i] == selectedType)
                    ? Texture.MANAGER_WIDGET_BACKGROUND_RED
                    : Texture.MANAGER_WIDGET_BACKGROUND;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, backgroundTexture, rowX, rowY, rowWidth, optionHeight);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(OVERRIDE_TYPES[i].getDisplayName()),
                            x + OPTION_HORIZONTAL_PADDING + ARROW_PADDING * 2 + ARROW_WIDTH,
                            rowY + optionHeight / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
    }

    // Render this as a polygon, otherwise it looks bad.
    private void renderArrow(GuiGraphics guiGraphics) {
        float cx = x + ARROW_PADDING + ARROW_WIDTH / 2f;
        float cy = y + collapsedHeight / 2f;
        float half = 3.2f;

        List<Vector2f> vertices = expanded
                ? List.of(
                        new Vector2f(cx - half, cy - half),
                        new Vector2f(cx + half, cy - half),
                        new Vector2f(cx, cy + half))
                : List.of(
                        new Vector2f(cx - half, cy - half),
                        new Vector2f(cx - half, cy + half),
                        new Vector2f(cx + half, cy));

        RenderUtils.drawPolygon(guiGraphics, CommonColors.WHITE, CustomColor.NONE, 0f, vertices);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseOverArrow(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            toggleExpanded();
            return true;
        }

        if (expanded) {
            OverrideType clicked = getOptionAt(event.x(), event.y());
            if (clicked != null) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                selectedType = clicked;
                parent.setSelectedOverrideType(clicked);
                toggleExpanded();
                return true;
            }
        }

        return false;
    }

    private void toggleExpanded() {
        expanded = !expanded;
        this.setHeight(getTotalHeight());
    }

    private int getTotalHeight() {
        return collapsedHeight + (expanded ? getOptionsAreaHeight() : 0);
    }

    private int getOptionsAreaHeight() {
        return OVERRIDE_TYPES.length * optionHeight
                + (OVERRIDE_TYPES.length + 1) * OPTION_VERTICAL_SPACING
                + OPTION_VERTICAL_SPACING;
    }

    private int getOptionRowY(int index) {
        return y + collapsedHeight + (index + 1) * OPTION_VERTICAL_SPACING + index * optionHeight;
    }

    private OverrideType getOptionAt(double mouseX, double mouseY) {
        for (int i = 0; i < OVERRIDE_TYPES.length; i++) {
            int rowY = getOptionRowY(i);
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    x + OPTION_HORIZONTAL_PADDING,
                    x + this.width - OPTION_HORIZONTAL_PADDING - 1,
                    rowY,
                    rowY + optionHeight - 1)) {
                return OVERRIDE_TYPES[i];
            }
        }

        return null;
    }

    public boolean isMouseOverAnyOption(double mouseX, double mouseY) {
        return expanded && getOptionAt(mouseX, mouseY) != null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isMouseOverArrow(double mouseX, double mouseY) {
        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                x + ARROW_PADDING,
                x + ARROW_PADDING + ARROW_WIDTH - 1,
                y + ARROW_HEIGHT / 2,
                y + collapsedHeight - ARROW_HEIGHT / 2 - 1);
    }
}

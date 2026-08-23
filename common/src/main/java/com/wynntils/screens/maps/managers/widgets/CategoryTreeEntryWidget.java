/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.type.CategoryTreeNode;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class CategoryTreeEntryWidget extends AbstractWidget {
    public static final int ROW_HEIGHT = 14;
    public static final int INDENT_WIDTH = 10;
    public static final int ARROW_WIDTH = 8;
    public static final int ARROW_ICON_GAP = 4;
    public static final int ICON_SIZE = 12;
    public static final int ICON_TEXT_GAP = 3;
    public static final int HORIZONTAL_PADDING = 2;

    private static final CustomColor HOVER_HIGHLIGHT = CommonColors.GRAY.withAlpha(0.35f);
    private static final CustomColor SELECTED_HIGHLIGHT = CustomColor.fromInt(0xbf3b46).withAlpha(0.35f);

    private final CategoryTreeNode node;
    private final int column;
    private final boolean[] siblingContinues;
    private boolean expanded;
    private boolean selected;

    private int x;
    private int y;

    private final Runnable onToggleExpand;
    private final Runnable onSelect;

    public CategoryTreeEntryWidget(
            int x,
            int y,
            int width,
            CategoryTreeNode node,
            int column,
            boolean[] siblingContinues,
            boolean expanded,
            boolean selected,
            Runnable onToggleExpand,
            Runnable onSelect) {
        super(x, y, width, ROW_HEIGHT, Component.literal(""));
        this.x = x;
        this.y = y;
        this.node = node;
        this.column = column;
        this.siblingContinues = siblingContinues;
        this.expanded = expanded;
        this.selected = selected;
        this.onToggleExpand = onToggleExpand;
        this.onSelect = onSelect;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.x = x;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.y = y;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getColumn() {
        return column;
    }

    public boolean[] getSiblingContinues() {
        return siblingContinues;
    }

    public CategoryTreeNode getNode() {
        return node;
    }

    public int getArrowX() {
        return x;
    }


    public int getIconX() {
        return x + ARROW_WIDTH + ARROW_ICON_GAP;
    }

    public int getContentX() {
        return node.isLeaf() ? getIconX() : x;
    }

    public int getContentWidth() {
        int textWidth = FontRenderer.getInstance().getFont().width(node.getName());
        int base = ICON_SIZE + ICON_TEXT_GAP + textWidth;
        return node.isLeaf() ? base : ARROW_WIDTH + ARROW_ICON_GAP + base;
    }

    public int computeContentWidth() {
        int textWidth = FontRenderer.getInstance().getFont().width(node.getName());
        return ARROW_WIDTH + ARROW_ICON_GAP + ICON_SIZE + ICON_TEXT_GAP + textWidth;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int leftPadding = node.isLeaf() ? HORIZONTAL_PADDING : 0;
        int contentX = getContentX() - leftPadding;
        int contentWidth = getContentWidth() + leftPadding + HORIZONTAL_PADDING;

        if (selected) {
            RenderUtils.drawRect(guiGraphics, SELECTED_HIGHLIGHT, contentX, y, contentWidth, ROW_HEIGHT);
        } else if (isHovered) {
            RenderUtils.drawRect(guiGraphics, HOVER_HIGHLIGHT, contentX, y, contentWidth, ROW_HEIGHT);
        }

        boolean mouseOverRow = MathUtils.isInside(
                mouseX,
                mouseY,
                contentX,
                contentX + contentWidth - 1,
                y,
                y + ROW_HEIGHT - 1);

        if (mouseOverRow) {
            handleCursor(guiGraphics);
        }

        if (!node.isLeaf()) {
            renderArrow(guiGraphics, getArrowX());
        }

        renderIcon(guiGraphics, getIconX(), node.isLeaf());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(node.getName()),
                        getIconX() + ICON_SIZE + ICON_TEXT_GAP,
                        y + ROW_HEIGHT / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    // Render this as a polygon, otherwise it looks bad.
    private void renderArrow(GuiGraphics guiGraphics, int arrowX) {
        float cx = arrowX + ARROW_WIDTH / 2f;
        float cy = y + ROW_HEIGHT / 2f;
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

    private void renderIcon(GuiGraphics guiGraphics, int iconX, boolean isLeaf) {
        if (isLeaf) {
            RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_FILE_ICON, iconX, y + (ROW_HEIGHT - ICON_SIZE) / 2f);
        } else {
            RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_FOLDER_ICON, iconX, y + (ROW_HEIGHT - ICON_SIZE) / 2f);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (!isMouseOver(event.x(), event.y())) return false;

        // Arrow click (only for non-leaf nodes)
        if (!node.isLeaf() && isMouseOverArrow(event.x(), event.y())) {
            onToggleExpand.run();
            return true;
        }

        onSelect.run();
        return true;
    }

    private boolean isMouseOverArrow(double mouseX, double mouseY) {
        return MathUtils.isInside((int) mouseX, (int) mouseY, x, x + ARROW_WIDTH, y, y + ROW_HEIGHT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

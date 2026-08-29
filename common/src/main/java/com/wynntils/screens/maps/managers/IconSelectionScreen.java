/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IconSelectionScreen extends WynntilsScreen {
    private static final int GRID_COLUMNS = 7;
    private static final int CELL_SIZE = 28;
    private static final int GRID_WIDTH_BUFFER = 24;

    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;
    private static final int BUTTON_TOP_GAP = 5;

    private final CategoryManagementScreen previousScreen;
    private final Consumer<MapIcon> onIconSelect;

    private String tempSelectedIconId;

    public IconSelectionScreen(
            CategoryManagementScreen previousScreen, Consumer<MapIcon> onIconSelect, String currentIconId) {
        super(Component.literal("Icon Selection Screen"));
        this.previousScreen = previousScreen;
        this.onIconSelect = onIconSelect;
        this.tempSelectedIconId = currentIconId;
    }

    @Override
    protected void doInit() {
        super.doInit();

        int gridWidth = GRID_COLUMNS * CELL_SIZE + GRID_WIDTH_BUFFER;
        int gridHeight = GRID_COLUMNS * CELL_SIZE;
        int gridX = (this.width - gridWidth) / 2;
        int gridY = (this.height - gridHeight) / 2;

        this.addRenderableWidget(new IconGridScrollWidget(
                gridX,
                gridY,
                gridWidth,
                gridHeight,
                icon -> tempSelectedIconId = (icon == null) ? MapIcon.NO_ICON_ID : icon.getIconId(),
                () -> tempSelectedIconId));

        this.addRenderableWidget(new StyledButton(
                (this.width / 2) - BUTTON_WIDTH - BUTTON_GAP / 2,
                gridY + gridHeight + BUTTON_TOP_GAP,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconSelectionScreen.cancel"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onCancel));

        this.addRenderableWidget(new StyledButton(
                (this.width / 2) + BUTTON_GAP / 2,
                gridY + gridHeight + BUTTON_TOP_GAP,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconSelectionScreen.save"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                this::onSave));
    }

    private void onCancel() {
        this.onClose();
    }

    private void onSave() {
        if (tempSelectedIconId != null && !tempSelectedIconId.isEmpty()) {
            if (tempSelectedIconId.equals(MapIcon.NO_ICON_ID)) {
                onIconSelect.accept(null);
            } else {
                Services.MapData.getIcon(tempSelectedIconId).ifPresent(onIconSelect);
            }
        }
        this.onClose();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }

    private static class StyledButton extends AbstractWidget {
        private final Texture backgroundTexture;
        private final Runnable onClick;

        StyledButton(
                int x, int y, int width, int height, Component message, Texture backgroundTexture, Runnable onClick) {
            super(x, y, width, height, message);
            this.backgroundTexture = backgroundTexture;
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.isHovered) {
                handleCursor(guiGraphics);
            }

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, backgroundTexture, getX(), getY(), getWidth(), getHeight());

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(getMessage().getString()),
                            getX() + getWidth() / 2f,
                            getY() + getHeight() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
            if (!this.isMouseOver(event.x(), event.y())) return false;

            this.playDownSound(McUtils.mc().getSoundManager());
            onClick.run();
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private static class IconGridRowWidget extends AbstractOptionWidget<List<MapIcon>> {
        private final List<MapIcon> icons;
        private final Consumer<MapIcon> onIconSelected;
        private final Supplier<String> selectedIconIdSupplier;

        IconGridRowWidget(
                List<MapIcon> icons, Consumer<MapIcon> onIconSelected, Supplier<String> selectedIconIdSupplier) {
            super(Component.literal("icon row"), Component.empty(), 0, null, null);
            this.icons = icons;
            this.onIconSelected = onIconSelected;
            this.selectedIconIdSupplier = selectedIconIdSupplier;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            String selectedIconId = selectedIconIdSupplier.get();

            for (int i = 0; i < icons.size(); i++) {
                MapIcon icon = icons.get(i);

                int cellX = getX() + i * CELL_SIZE;
                int cellY = getY();

                String iconId = icon == null ? MapIcon.NO_ICON_ID : icon.getIconId();
                boolean selected = iconId.equals(selectedIconId);
                boolean hovered =
                        MathUtils.isInside(mouseX, mouseY, cellX, cellX + CELL_SIZE - 1, cellY, cellY + CELL_SIZE - 1);

                if (selected) {
                    RenderUtils.drawRect(
                            guiGraphics, CustomColor.fromInt(0x8033cc33), cellX, cellY, CELL_SIZE, CELL_SIZE);
                    handleCursor(guiGraphics);
                } else if (hovered) {
                    RenderUtils.drawRect(
                            guiGraphics, CustomColor.fromInt(0x40ffffff), cellX, cellY, CELL_SIZE, CELL_SIZE);
                    handleCursor(guiGraphics);
                }

                if (icon == null) {
                    FontRenderer.getInstance()
                            .renderText(
                                    guiGraphics,
                                    StyledText.fromComponent(Component.translatable(
                                            "screens.wynntils.map.managers.categoryManager.iconOptionWidget.noneText")),
                                    cellX + CELL_SIZE / 2f,
                                    cellY + CELL_SIZE / 2f,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.MIDDLE,
                                    TextShadow.NORMAL);
                } else {
                    int iconWidth = icon.getWidth();
                    int iconHeight = icon.getHeight();
                    int drawX = cellX + (CELL_SIZE - iconWidth) / 2;
                    int drawY = cellY + (CELL_SIZE - iconHeight) / 2;

                    RenderUtils.drawTexturedRect(
                            guiGraphics,
                            RenderPipelines.GUI_TEXTURED,
                            icon.getIdentifier(),
                            CommonColors.WHITE,
                            drawX,
                            drawY,
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
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
            if (!this.isMouseOver(event.x(), event.y())) return false;

            int index = (int) ((event.x() - getX()) / CELL_SIZE);
            if (index < 0 || index >= icons.size()) return false;

            this.playDownSound(McUtils.mc().getSoundManager());
            onIconSelected.accept(icons.get(index));
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public int getHeight() {
            return CELL_SIZE;
        }
    }

    private static class IconGridScrollWidget extends ScrollBarWidget {
        private final List<IconGridRowWidget> rows;

        IconGridScrollWidget(
                int x,
                int y,
                int width,
                int height,
                Consumer<MapIcon> onIconSelected,
                Supplier<String> selectedIconIdSupplier) {
            super(x, y, width, height, null);

            List<MapIcon> icons = new ArrayList<>();
            icons.add(null); // "None" option
            icons.addAll(Services.MapData.getIcons()
                    .sorted(Comparator.comparing(MapIcon::getIconId))
                    .toList());

            this.rows = chunkIntoRows(icons, onIconSelected, selectedIconIdSupplier);
        }

        private static List<IconGridRowWidget> chunkIntoRows(
                List<MapIcon> icons, Consumer<MapIcon> onIconSelected, Supplier<String> selectedIconIdSupplier) {
            List<IconGridRowWidget> rows = new ArrayList<>();
            for (int i = 0; i < icons.size(); i += GRID_COLUMNS) {
                List<MapIcon> rowIcons = new ArrayList<>(icons.subList(i, Math.min(i + GRID_COLUMNS, icons.size())));
                rows.add(new IconGridRowWidget(rowIcons, onIconSelected, selectedIconIdSupplier));
            }
            return rows;
        }

        @Override
        protected List<IconGridRowWidget> getWidgets() {
            return rows;
        }

        private boolean forEachVisibleRow(RowAction action) {
            int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
            int viewportBottom = viewportTop + getViewportHeight();
            int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
            int contentWidth = getViewportWidth();
            int currentY = viewportTop - scrollOffsetY;

            for (IconGridRowWidget row : rows) {
                int rowHeight = row.getHeight();
                if (currentY + rowHeight >= viewportTop && currentY <= viewportBottom) {
                    row.setX(viewportLeft);
                    row.setY(currentY);
                    row.setWidth(contentWidth);
                    if (action.apply(row)) {
                        return true;
                    }
                }
                currentY += rowHeight;
            }
            return false;
        }

        @Override
        protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            forEachVisibleRow(row -> {
                row.render(guiGraphics, mouseX, mouseY, partialTick);
                return false;
            });
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (super.mouseClicked(event, isDoubleClick)) {
                return true;
            }

            int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
            int viewportBottom = viewportTop + getViewportHeight();
            int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
            int viewportRight = viewportLeft + getViewportWidth();

            if (!(event.x() >= viewportLeft
                    && event.x() < viewportRight
                    && event.y() >= viewportTop
                    && event.y() < viewportBottom)) {
                return false;
            }

            return forEachVisibleRow(row -> row.mouseClicked(event, isDoubleClick));
        }

        @FunctionalInterface
        private interface RowAction {
            boolean apply(IconGridRowWidget row);
        }
    }
}

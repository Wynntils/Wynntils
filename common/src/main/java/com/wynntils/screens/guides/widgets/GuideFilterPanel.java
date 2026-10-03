/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GuideFilterPanel extends AbstractWidget {
    private static final int PADDING = 5;
    private static final int SCROLL_AREA_TOP_PADDING = 12;
    private static final int WIDGET_TOP_PADDING = 14;
    private static final int SCROLL_AREA_BOTTOM_PADDING = 2;
    private static final int WIDGET_SPACING = 4;
    private static final float SCROLL_FACTOR = 10f;

    private List<? extends AbstractWidget> filterWidgets;
    private List<GuideFilterCategoryShortcut> categoryShortcuts = new ArrayList<>();

    private boolean draggingScroll = false;
    private int scrollOffset = 0;
    private float scrollY;

    public GuideFilterPanel(int x, int y, int width, int height, List<? extends AbstractWidget> filterWidgets) {
        super(x, y, width, height, Component.empty());

        this.filterWidgets = filterWidgets;

        updateLayout();
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.GUIDE_BACKGROUND, getX(), getY(), getWidth(), getHeight());

        RenderUtils.enableScissor(guiGraphics, getX(), getScrollAreaStartY() + 2, 135, getScrollAreaHeight());
        for (AbstractWidget filterWidget : filterWidgets) {
            filterWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
        RenderUtils.disableScissor(guiGraphics);

        renderCategoryShortcuts(guiGraphics, mouseX, mouseY);
        renderScroll(guiGraphics, mouseX, mouseY);
    }

    public void updateLayout() {
        scrollOffset = MathUtils.clamp(scrollOffset, 0, getMaxScrollOffset());

        int widgetX = getX() + PADDING;
        int currentY = getY() + WIDGET_TOP_PADDING - scrollOffset;
        int scrollAreaStartY = getScrollAreaStartY();
        int scrollAreaEndY = scrollAreaStartY + getScrollAreaHeight();

        Map<String, List<String>> shortcutProviders = new LinkedHashMap<>();
        Map<String, Integer> shortcutOffsets = new LinkedHashMap<>();

        int contentOffset = 0;

        for (AbstractWidget filterWidget : filterWidgets) {
            filterWidget.setPosition(widgetX, currentY);
            filterWidget.visible = currentY + filterWidget.getHeight() >= scrollAreaStartY && currentY < scrollAreaEndY;

            Optional<String> category = getWidgetCategory(filterWidget);
            if (category.isPresent()) {
                shortcutOffsets.putIfAbsent(category.get(), contentOffset);

                shortcutProviders
                        .computeIfAbsent(category.get(), ignored -> new ArrayList<>())
                        .addAll(getWidgetProviders(filterWidget).stream()
                                .map(ItemStatProvider::getDisplayName)
                                .toList());
            }

            currentY += filterWidget.getHeight() + WIDGET_SPACING;
            contentOffset += filterWidget.getHeight() + WIDGET_SPACING;
        }

        categoryShortcuts = shortcutProviders.entrySet().stream()
                .map(entry -> new GuideFilterCategoryShortcut(
                        entry.getKey(), shortcutOffsets.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    private void updateScrollY() {
        if (getMaxScrollOffset() <= 0) return;

        scrollY = getScrollAreaStartY()
                + MathUtils.map(
                        scrollOffset,
                        0,
                        getMaxScrollOffset(),
                        0,
                        getScrollAreaHeight() - Texture.SCROLL_BUTTON.height());
    }

    private void renderScroll(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (getMaxScrollOffset() <= 0) return;

        updateScrollY();

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, getScrollBarX(), scrollY);

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (isOverScrollBar(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    private void renderCategoryShortcuts(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (getMaxScrollOffset() <= 0) return;

        boolean overScrollBar = isOverScrollBar(mouseX, mouseY);

        for (GuideFilterCategoryShortcut shortcut : categoryShortcuts) {
            int shortcutY = getShortcutY(shortcut.getContentOffset());
            RenderUtils.drawRect(
                    guiGraphics,
                    CommonColors.WHITE.withAlpha(
                            !overScrollBar && isOverShortcut(shortcut, mouseX, mouseY) ? 0.9f : 0.55f),
                    getScrollBarX(),
                    shortcutY,
                    Texture.SCROLL_BUTTON.width(),
                    2);

            if (!overScrollBar && isOverShortcut(shortcut, mouseX, mouseY)) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                RenderUtils.renderTooltip(guiGraphics, shortcut.getTooltip(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        updateScrollY();

        if (getMaxScrollOffset() > 0) {
            if (isOverScrollBar((int) event.x(), (int) event.y())) {
                draggingScroll = true;
                return true;
            }

            for (GuideFilterCategoryShortcut shortcut : categoryShortcuts) {
                if (isOverShortcut(shortcut, (int) event.x(), (int) event.y())) {
                    scroll(Math.min(shortcut.getContentOffset(), getMaxScrollOffset()));
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    return true;
                }
            }
        }

        for (AbstractWidget filterWidget : filterWidgets) {
            if (filterWidget.visible && filterWidget.isMouseOver(event.x(), event.y())) {
                int oldHeight = filterWidget.getHeight();
                boolean clicked = filterWidget.mouseClicked(event, isDoubleClick);

                updateLayout();
                if (filterWidget.getHeight() > oldHeight) {
                    scrollToWidgetBottom(filterWidget);
                }

                return clicked;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!draggingScroll) {
            for (AbstractWidget filterWidget : filterWidgets) {
                if (filterWidget.visible && filterWidget.isMouseOver(event.x(), event.y())) {
                    return filterWidget.mouseDragged(event, dragX, dragY);
                }
            }

            return false;
        }

        int scrollAreaStartY = getScrollAreaStartY();
        int scrollAreaHeight = getScrollAreaHeight() - Texture.SCROLL_BUTTON.height();

        float thumbTop = (float) event.y() - Texture.SCROLL_BUTTON.height() / 2f;

        int newOffset = Math.round(MathUtils.map(
                thumbTop, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

        newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

        scroll(newOffset);

        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (AbstractWidget filterWidget : filterWidgets) {
            filterWidget.mouseReleased(event);
        }

        draggingScroll = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (getMaxScrollOffset() <= 0) return false;

        scroll(scrollOffset + (int) (-deltaY * SCROLL_FACTOR));

        return true;
    }

    public List<? extends AbstractWidget> getFilterWidgets() {
        return filterWidgets;
    }

    public void setFilterWidgets(List<? extends AbstractWidget> filterWidgets) {
        this.filterWidgets = filterWidgets;
        scrollOffset = 0;
        updateLayout();
    }

    private void scroll(int newOffset) {
        scrollOffset = MathUtils.clamp(newOffset, 0, getMaxScrollOffset());
        updateLayout();
    }

    private void scrollToWidgetBottom(AbstractWidget filterWidget) {
        int widgetBottom = filterWidget.getY() + filterWidget.getHeight();
        int scrollAreaEndY = getScrollAreaStartY() + getScrollAreaHeight();

        if (widgetBottom > scrollAreaEndY) {
            scroll(scrollOffset + widgetBottom - scrollAreaEndY);
        }
    }

    private int getContentHeight() {
        if (filterWidgets.isEmpty()) return 0;

        int contentHeight = -WIDGET_SPACING;

        for (AbstractWidget filterWidget : filterWidgets) {
            contentHeight += filterWidget.getHeight() + WIDGET_SPACING;
        }

        return contentHeight;
    }

    private int getMaxScrollOffset() {
        int contentStartOffset = WIDGET_TOP_PADDING - SCROLL_AREA_TOP_PADDING;

        return Math.max(0, contentStartOffset + getContentHeight() - getScrollAreaHeight());
    }

    private boolean isOverScrollBar(int mouseX, int mouseY) {
        return MathUtils.isInside(
                mouseX, mouseY, getScrollBarX(), getScrollBarX() + Texture.SCROLL_BUTTON.width(), (int) scrollY, (int)
                        (scrollY + Texture.SCROLL_BUTTON.height()));
    }

    private int getScrollBarX() {
        return getX() + getWidth() - Texture.SCROLL_BUTTON.width() - 2;
    }

    private int getScrollAreaStartY() {
        return getY() + SCROLL_AREA_TOP_PADDING;
    }

    private int getScrollAreaHeight() {
        return getHeight() - SCROLL_AREA_TOP_PADDING - SCROLL_AREA_BOTTOM_PADDING;
    }

    private int getShortcutY(int contentOffset) {
        int clampedOffset = MathUtils.clamp(contentOffset, 0, getMaxScrollOffset());

        return getScrollAreaStartY()
                + Math.round(MathUtils.map(clampedOffset, 0, getMaxScrollOffset(), 0, getScrollAreaHeight() - 3));
    }

    private boolean isOverShortcut(GuideFilterCategoryShortcut shortcut, int mouseX, int mouseY) {
        int shortcutY = getShortcutY(shortcut.getContentOffset());

        return MathUtils.isInside(
                mouseX,
                mouseY,
                getScrollBarX(),
                getScrollBarX() + Texture.SCROLL_BUTTON.width(),
                shortcutY - 1,
                shortcutY + 3);
    }

    private Optional<String> getWidgetCategory(AbstractWidget widget) {
        return getWidgetProviders(widget).stream()
                .map(ItemStatProvider::getGuideCategory)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private List<ItemStatProvider<?>> getWidgetProviders(AbstractWidget widget) {
        if (widget instanceof GuideFilterWidget guideFilterWidget) {
            return guideFilterWidget.getProviders();
        }

        if (widget instanceof GuideSortWidget guideSortWidget) {
            return List.of(guideSortWidget.getProvider());
        }

        return List.of();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

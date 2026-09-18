/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

final class OverlayHelpTooltip {
    private static final int MAX_WIDTH = 200;
    private static final int MAX_VISIBLE_LINES = 8;
    private static final int SCREEN_MARGIN = 24;
    private static final int SCROLLBAR_WIDTH = 2;
    private static final int SCROLLBAR_GUTTER_WIDTH = 12;

    private int scrollOffset;
    private int maxScrollOffset;
    private List<Component> wrappedLines = List.of();
    private int visibleLineCount;
    private boolean showScrollHint;

    public void reset() {
        scrollOffset = 0;
        maxScrollOffset = 0;
        wrappedLines = List.of();
        visibleLineCount = 0;
        showScrollHint = false;
    }

    public boolean scroll(
            List<Component> content, Font font, int screenWidth, int screenHeight, double scrollDirection) {
        prepare(content, font, screenWidth, screenHeight);
        if (maxScrollOffset == 0) return true;

        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset - (int) Math.signum(scrollDirection)));
        return true;
    }

    public void render(
            GuiGraphics graphics,
            List<Component> content,
            Font font,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY) {
        prepare(content, font, screenWidth, screenHeight);

        List<Component> visibleLines =
                new ArrayList<>(wrappedLines.subList(scrollOffset, scrollOffset + visibleLineCount));
        int textWidth = TooltipUtils.getTooltipWidth(TooltipUtils.getClientTooltipComponent(wrappedLines), font);
        if (showScrollHint) {
            List<Component> scrollHint = getScrollHint(getMaxTextWidth(screenWidth));
            visibleLines.addAll(scrollHint);
            textWidth = Math.max(
                    textWidth, TooltipUtils.getTooltipWidth(TooltipUtils.getClientTooltipComponent(scrollHint), font));
        }

        int scrollbarGutterWidth = maxScrollOffset > 0 ? SCROLLBAR_GUTTER_WIDTH : 0;
        int tooltipWidth = textWidth + scrollbarGutterWidth;
        List<ClientTooltipComponent> tooltipComponents = TooltipUtils.getClientTooltipComponent(visibleLines).stream()
                .<ClientTooltipComponent>map(
                        component -> new FixedWidthTooltipComponent(component, tooltipWidth, scrollbarGutterWidth))
                .toList();
        Vector2i tooltipPosition = new Vector2i();
        ClientTooltipPositioner positioner =
                (width, height, tooltipMouseX, tooltipMouseY, contentWidth, contentHeight) -> {
                    Vector2ic position = DefaultTooltipPositioner.INSTANCE.positionTooltip(
                            width, height, tooltipMouseX, tooltipMouseY, contentWidth, contentHeight);
                    int maxX = Math.max(12, width - contentWidth - 12);
                    int maxY = Math.max(12, height - contentHeight - 12);
                    int x = Math.max(12, Math.min(position.x(), maxX));
                    int y = Math.max(12, Math.min(position.y(), maxY));
                    tooltipPosition.set(x, y);
                    return tooltipPosition;
                };

        graphics.renderTooltip(font, tooltipComponents, mouseX, mouseY, positioner, null);

        if (maxScrollOffset > 0) {
            int tooltipHeight = TooltipUtils.getTooltipHeight(tooltipComponents);
            renderScrollbar(
                    graphics,
                    tooltipPosition.x(),
                    tooltipPosition.y(),
                    tooltipWidth,
                    tooltipHeight,
                    scrollbarGutterWidth);
        }
    }

    private void prepare(List<Component> content, Font font, int screenWidth, int screenHeight) {
        int maxTextWidth = getMaxTextWidth(screenWidth);
        wrappedLines = ComponentUtils.wrapTooltips(content, maxTextWidth);

        int lineHeight = wrappedLines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .mapToInt(component -> component.getHeight(font))
                .max()
                .orElse(font.lineHeight);
        int maxLinesForScreen = Math.max(1, (screenHeight - SCREEN_MARGIN) / Math.max(1, lineHeight));
        int maxVisibleLines = Math.min(MAX_VISIBLE_LINES, maxLinesForScreen);

        List<Component> scrollHint = getScrollHint(maxTextWidth);
        showScrollHint = wrappedLines.size() > maxVisibleLines && maxVisibleLines > scrollHint.size();
        int hintLineCount = showScrollHint ? scrollHint.size() : 0;
        visibleLineCount = Math.max(1, Math.min(wrappedLines.size(), maxVisibleLines - hintLineCount));
        maxScrollOffset = Math.max(0, wrappedLines.size() - visibleLineCount);
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
    }

    private List<Component> getScrollHint(int maxTextWidth) {
        return ComponentUtils.wrapTooltips(
                List.of(Component.translatable("screens.wynntils.overlayManagement.helpTooltip.scrollHint")
                        .withStyle(ChatFormatting.GRAY)),
                maxTextWidth);
    }

    private int getMaxTextWidth(int screenWidth) {
        return Math.max(1, Math.min(MAX_WIDTH, screenWidth - SCREEN_MARGIN - SCROLLBAR_GUTTER_WIDTH));
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int tooltipX,
            int tooltipY,
            int tooltipWidth,
            int tooltipHeight,
            int scrollbarGutterWidth) {
        int trackTop = tooltipY;
        int trackHeight = Math.max(1, tooltipHeight);
        int thumbHeight = Math.min(trackHeight, Math.max(6, trackHeight * visibleLineCount / wrappedLines.size()));
        int thumbTop = trackTop + (trackHeight - thumbHeight) * scrollOffset / Math.max(1, maxScrollOffset);
        int scrollbarX = tooltipX + tooltipWidth - scrollbarGutterWidth + (scrollbarGutterWidth - SCROLLBAR_WIDTH) / 2;

        graphics.fill(scrollbarX, trackTop, scrollbarX + SCROLLBAR_WIDTH, trackTop + trackHeight, 0x80000000);
        graphics.fill(scrollbarX, thumbTop, scrollbarX + SCROLLBAR_WIDTH, thumbTop + thumbHeight, 0xFFFFFFFF);
    }

    private record FixedWidthTooltipComponent(ClientTooltipComponent delegate, int width, int gutterWidth)
            implements ClientTooltipComponent {
        @Override
        public int getHeight(Font font) {
            return delegate.getHeight(font);
        }

        @Override
        public int getWidth(Font font) {
            return width;
        }

        @Override
        public void renderText(GuiGraphics graphics, Font font, int x, int y) {
            delegate.renderText(graphics, font, x, y);
        }

        @Override
        public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
            delegate.renderImage(font, x, y, Math.max(0, width - gutterWidth), height, graphics);
        }
    }
}

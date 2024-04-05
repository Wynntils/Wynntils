/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ListValueWidget extends GeneralValueWidget {
    private static final int MAX_OPTIONS_PER_PAGE = 6;
    private static final int SCROLLBAR_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_RENDER_X = 180;

    private final float translationX;
    private final float translationY;
    private final List<String> values;

    private boolean draggingScroll = false;
    private double currentUnusedScroll = 0;
    private int scrollOffset = 0;
    private int scrollRenderY;
    private List<String> enabledValues;

    public ListValueWidget(
            float translationX,
            float translationY,
            List<String> values,
            List<String> query,
            ItemFilterScreen filterScreen) {
        super(Component.literal("List Value Widget"), filterScreen);

        this.values = values;
        this.translationX = translationX;
        this.translationY = translationY;

        // Get which values are enabled from the query and compare against the known valid values
        enabledValues = this.values.stream()
                .filter(v -> query.stream().anyMatch(q -> q.equalsIgnoreCase(v)))
                .collect(Collectors.toList());

        populateValues();
        updateQuery();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (values.size() > MAX_OPTIONS_PER_PAGE) {
            renderScrollBar(guiGraphics.pose());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll && values.size() > MAX_OPTIONS_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    getX() + SCROLLBAR_RENDER_X,
                    getX() + SCROLLBAR_RENDER_X + SCROLLBAR_WIDTH,
                    scrollRenderY,
                    scrollRenderY + SCROLLBAR_HEIGHT)) {
                draggingScroll = true;

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int renderY = getY() + 5;
            int scrollAreaStartY = renderY + 10;

            int newValue = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + MAX_OPTIONS_PER_PAGE * 22 - SCROLLBAR_HEIGHT,
                    0,
                    Math.max(0, values.size() - MAX_OPTIONS_PER_PAGE)));

            scroll(newValue - scrollOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (values.size() > MAX_OPTIONS_PER_PAGE) {
            if (Math.abs(deltaY) == 1.0) {
                scroll((int) -deltaY);
                return true;
            }

            // Account for scrollpad
            currentUnusedScroll -= deltaY / 5d;

            if (Math.abs(currentUnusedScroll) < 1) return true;

            int scroll = (int) (currentUnusedScroll);
            currentUnusedScroll = currentUnusedScroll % 1;

            scroll(scroll);
        }

        return true;
    }

    private void populateValues() {
        widgets = new ArrayList<>();

        int currentValue;
        int renderY = getY() + 2;

        for (int i = 0; i < MAX_OPTIONS_PER_PAGE; i++) {
            currentValue = i + scrollOffset;

            if (currentValue > values.size() - 1) {
                break;
            }

            widgets.add(new ValueWidget(
                    getX() + 5,
                    renderY,
                    150,
                    20,
                    values.get(currentValue),
                    enabledValues.contains(values.get(currentValue))));

            renderY += 24;
        }
    }

    private void renderScrollBar(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack,
                CommonColors.LIGHT_GRAY,
                getX() + SCROLLBAR_RENDER_X,
                getY(),
                0,
                SCROLLBAR_WIDTH,
                MAX_OPTIONS_PER_PAGE * 24);

        scrollRenderY = (int) (getY()
                + MathUtils.map(
                        scrollOffset,
                        0,
                        values.size() - MAX_OPTIONS_PER_PAGE,
                        0,
                        MAX_OPTIONS_PER_PAGE * 24 - SCROLLBAR_HEIGHT));

        RenderUtils.drawRect(
                poseStack,
                draggingScroll ? CommonColors.BLACK : CommonColors.GRAY,
                getX() + SCROLLBAR_RENDER_X,
                scrollRenderY,
                0,
                SCROLLBAR_WIDTH,
                SCROLLBAR_HEIGHT);
    }

    private void scroll(int delta) {
        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, values.size() - MAX_OPTIONS_PER_PAGE));

        populateValues();
    }

    private void addValue(String valueName) {
        enabledValues.add(valueName);
    }

    private void removeValue(String valueName) {
        enabledValues.remove(valueName);
    }

    @Override
    public void updateValues(List<String> query) {
        // Update the list of enabled values based on the ItemSearchWidget
        enabledValues = this.values.stream()
                .filter(v -> query.stream().anyMatch(q -> q.equalsIgnoreCase(v)))
                .collect(Collectors.toList());

        populateValues();
    }

    @Override
    protected void updateQuery() {
        // Remove all existing filters for the current provider
        filterScreen.removeFilter(filterScreen.getSelectedProvider());

        // If there are no enabled values, there is no filter for this provider
        if (enabledValues.isEmpty()) {
            query = "";
            return;
        }

        // For each enabled value add them as a new filter for the current provider
        for (String enabledValue : enabledValues) {
            filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), enabledValue));
        }
    }

    private class ValueWidget extends AbstractWidget {
        private static final CustomColor UNUSED_COLOR = new CustomColor(116, 0, 0, 255);
        private static final CustomColor UNUSED_COLOR_BORDER = new CustomColor(220, 0, 0, 255);
        private static final CustomColor USED_COLOR = new CustomColor(0, 116, 0, 255);
        private static final CustomColor USED_COLOR_BORDER = new CustomColor(0, 220, 0, 255);

        private final String valueName;
        private final WynntilsCheckbox usedCheckbox;

        private boolean used;

        private ValueWidget(int x, int y, int width, int height, String valueName, boolean used) {
            super(x, y, width, height, Component.literal(valueName));

            this.usedCheckbox = new WynntilsCheckbox(
                    x + width - 16, y + 2, 16, 16, Component.literal(""), used, 0, (b -> toggleUsed()));

            this.valueName = valueName;
            this.used = used;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            PoseStack poseStack = guiGraphics.pose();

            RenderUtils.drawRect(poseStack, getRectColor().withAlpha(100), getX(), getY(), 0, width - 18, height);

            RenderUtils.drawRectBorders(
                    poseStack, getBorderColor(), getX(), getY(), getX() + width - 18, getY() + height, 1, 2);

            FontRenderer.getInstance()
                    .renderScrollingString(
                            poseStack,
                            StyledText.fromString(valueName),
                            getX() + 2,
                            getY() + (height / 2f),
                            width - 4,
                            translationX,
                            translationY,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1.0f);

            usedCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (usedCheckbox.isMouseOver(mouseX, mouseY)) {
                return usedCheckbox.mouseClicked(mouseX, mouseY, button);
            }

            return false;
        }

        private void toggleUsed() {
            if (used) {
                removeValue(valueName);
            } else {
                addValue(valueName);
            }

            used = !used;

            updateQuery();
        }

        private CustomColor getRectColor() {
            return used ? USED_COLOR : UNUSED_COLOR;
        }

        private CustomColor getBorderColor() {
            return used ? USED_COLOR_BORDER : UNUSED_COLOR_BORDER;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}

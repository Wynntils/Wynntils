/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class StringValueWidget extends GeneralValueWidget {
    private static final int MAX_OPTIONS_PER_PAGE = 5;

    private StringAnyWidget anyWidget;
    private Button addFilterButton;
    private boolean draggingScroll = false;
    private double currentUnusedScroll = 0;
    private int scrollOffset = 0;
    private int scrollRenderY;
    private List<StatProviderAndFilterPair> filters;

    // situations.
    public StringValueWidget(ItemStatProvider<?> itemStatProvider, ItemFilterScreen filterScreen) {
        super(Component.literal("String Value Widget"), itemStatProvider, filterScreen);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (addFilterButton != null) {
            addFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
        } else if (anyWidget != null) {
            anyWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (filters.size() > MAX_OPTIONS_PER_PAGE) {
            renderScrollBar(guiGraphics.pose());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (anyWidget != null && anyWidget.isMouseOver(mouseX, mouseY)) {
            return anyWidget.mouseClicked(mouseX, mouseY, button);
        } else if (addFilterButton != null && addFilterButton.isMouseOver(mouseX, mouseY)) {
            return addFilterButton.mouseClicked(mouseX, mouseY, button);
        }

        if (!draggingScroll && filters.size() > MAX_OPTIONS_PER_PAGE) {
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
                    Math.max(0, filters.size() - MAX_OPTIONS_PER_PAGE)));

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

        if (filters.size() > MAX_OPTIONS_PER_PAGE) {
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

    private void populateWidgets() {
        widgets = new ArrayList<>();

        int currentValue;
        int renderY = getY() + 2;

        for (int i = 0; i < MAX_OPTIONS_PER_PAGE; i++) {
            currentValue = i + scrollOffset;

            if (currentValue > filters.size() - 1) break;

            if (filters.get(currentValue).statFilter() == null) {
                widgets.add(new StringWidget(getX(), renderY, "", false));
            } else {
                String currentInput = filters.get(currentValue).statFilter().asString();
                boolean strict = currentInput.startsWith("\"") && currentInput.endsWith("\"");

                if (strict) {
                    currentInput = currentInput.substring(1, currentInput.length() - 1);
                }

                widgets.add(new StringWidget(getX(), renderY, currentInput, strict));
            }

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
                        filters.size() - MAX_OPTIONS_PER_PAGE,
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
        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, filters.size() - MAX_OPTIONS_PER_PAGE));

        populateWidgets();
    }

    private void replaceFilter(String oldFilter, String newFilter) {
        filters = filters.stream()
                .filter(statPair -> {
                    if (statPair.statFilter() == null) return true;
                    return !statPair.statFilter().asString().equals(oldFilter);
                })
                .collect(Collectors.toList());

        Optional<StringStatFilter> stringStatFilterOpt =
                new StringStatFilter.StringStatFilterFactory().create(newFilter);

        if (stringStatFilterOpt.isEmpty()
                || stringStatFilterOpt.get().asString().isEmpty()) {
            updateQuery();
        } else {
            filters.add(new StatProviderAndFilterPair(itemStatProvider, stringStatFilterOpt.get()));

            updateQuery();
        }
    }

    private void removeAny() {
        filterScreen.setFiltersForProvider(itemStatProvider, null);
        anyWidget = null;

        addFilterButton = new Button.Builder(
                        Component.translatable("screens.wynntils.itemFilter.addNewFilter"), (button -> {
                            if (filters.size() > 1) {
                                this.filters.add(new StatProviderAndFilterPair(itemStatProvider, null));
                                populateWidgets();
                            }
                        }))
                .pos(getX() + 55, getY() + 125)
                .size(85, 20)
                .build();
    }

    @Override
    public void onFiltersChanged(List<StatProviderAndFilterPair> filters) {
        this.filters = filters == null ? new ArrayList<>() : new ArrayList<>(filters);

        for (StatProviderAndFilterPair filterPair : this.filters) {
            if (filterPair.statFilter() instanceof AnyStatFilters.AnyStringStatFilter) {
                anyWidget = new StringAnyWidget();
                widgets = new ArrayList<>();
                addFilterButton = null;
                return;
            }
        }

        anyWidget = null;

        addFilterButton = new Button.Builder(
                        Component.translatable("screens.wynntils.itemFilter.addNewFilter"), (button -> {
                            this.filters.add(new StatProviderAndFilterPair(itemStatProvider, null));
                            populateWidgets();
                        }))
                .pos(getX() + 55, getY() + 125)
                .size(85, 20)
                .build();

        if (filters.isEmpty()) {
            this.filters.add(new StatProviderAndFilterPair(itemStatProvider, null));
        }

        populateWidgets();
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilterPairs() {
        return filters;
    }

    private final class StringAnyWidget extends AbstractWidget {
        private final Button removeButton;

        private StringAnyWidget() {
            super(150, 30, 195, 145, Component.literal("String Any Widget"));

            removeButton = new Button.Builder(
                            Component.translatable("screens.wynntils.itemFilter.removeAny"), (button -> removeAny()))
                    .pos(getX() + 25, getY() + 95)
                    .size(145, 20)
                    .build();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            removeButton.render(guiGraphics, mouseX, mouseY, partialTick);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            StyledText.fromComponent(Component.translatable(
                                    "screens.wynntils.itemFilter.anyQueryInfo",
                                    itemStatProvider.getDisplayName(),
                                    itemStatProvider.getDisplayName())),
                            147,
                            345,
                            63,
                            123,
                            200,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (removeButton.isMouseOver(mouseX, mouseY)) {
                return removeButton.mouseClicked(mouseX, mouseY, button);
            }

            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private final class StringWidget extends AbstractWidget {
        private final Button removeButton;
        private final TextInputBoxWidget entryInput;
        private final WynntilsCheckbox strictCheckbox;

        private boolean strict;
        private String originalInput;

        private StringWidget(int x, int y, String input, boolean strict) {
            super(x, y, 160, 20, Component.literal("String Widget"));

            this.originalInput = input;
            this.strict = strict;

            // FIXME: Various crashing issues when typing.
            //  If widget already had an input, no issues until adding a new filter.
            //  If multiple widgets existed without clicking add new filter, typing in one causes
            //  duplicates to appear
            this.entryInput = new TextInputBoxWidget(
                    getX(),
                    getY(),
                    80,
                    20,
                    (s -> {
                        if (!s.equals(originalInput)) {
                            if (this.strict) {
                                String strictInput = s.isEmpty() ? "" : "\"" + s + "\"";
                                replaceFilter(originalInput, strictInput);
                                originalInput = strictInput;
                            } else {
                                replaceFilter(originalInput, s);
                                originalInput = s;
                            }
                        }
                    }),
                    filterScreen);

            entryInput.setTextBoxInput(input);

            this.strictCheckbox = new WynntilsCheckbox(
                    getX() + 85,
                    getY(),
                    20,
                    20,
                    Component.translatable("screens.wynntils.itemFilter.strict"),
                    strict,
                    40,
                    (button) -> {
                        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            this.strict = !this.strict;

                            if (entryInput.getTextBoxInput().isEmpty()) return;

                            String strictInput = entryInput.getTextBoxInput().isEmpty()
                                    ? ""
                                    : "\"" + entryInput.getTextBoxInput() + "\"";

                            if (this.strict) {
                                replaceFilter(originalInput, strictInput);
                                originalInput = strictInput;
                            } else {
                                replaceFilter(strictInput, entryInput.getTextBoxInput());
                                originalInput = entryInput.getTextBoxInput();
                            }
                        }
                    },
                    List.of(Component.translatable("screens.wynntils.itemFilter.strictTooltip")));

            // FIXME: Crashes after add new widget has been pressed more than once
            this.removeButton = new Button.Builder(
                            Component.literal("🗑"),
                            (button -> replaceFilter(
                                    this.strict
                                            ? "\"" + entryInput.getTextBoxInput() + "\""
                                            : entryInput.getTextBoxInput(),
                                    "")))
                    .pos(x + 140, y)
                    .size(20, 20)
                    .build();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            entryInput.render(guiGraphics, mouseX, mouseY, partialTick);
            strictCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
            removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (entryInput.isMouseOver(mouseX, mouseY)) {
                return entryInput.mouseClicked(mouseX, mouseY, button);
            } else if (strictCheckbox.isMouseOver(mouseX, mouseY)) {
                return strictCheckbox.mouseClicked(mouseX, mouseY, button);
            } else if (removeButton.isMouseOver(mouseX, mouseY)) {
                return removeButton.mouseClicked(mouseX, mouseY, button);
            }

            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (entryInput.isMouseOver(mouseX, mouseY)) {
                return entryInput.mouseReleased(mouseX, mouseY, button);
            } else if (strictCheckbox.isMouseOver(mouseX, mouseY)) {
                return strictCheckbox.mouseReleased(mouseX, mouseY, button);
            } else if (removeButton.isMouseOver(mouseX, mouseY)) {
                return removeButton.mouseReleased(mouseX, mouseY, button);
            }

            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}

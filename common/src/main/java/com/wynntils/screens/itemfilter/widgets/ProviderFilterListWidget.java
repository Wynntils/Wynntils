/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.type.NumericFilterWidgetFactory;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProviderFilterListWidget extends AbstractWidget {
    private static final float SCROLL_FACTOR = 10f;
    private static final int MAX_WIDGETS_PER_PAGE = 5;
    private static final int MAX_SELECTION_WIDGETS_PER_PAGE = 6;
    private static final int SCROLLBAR_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_RENDER_X = 184;

    private final ItemFilterScreen filterScreen;
    private final ItemStatProvider<?> provider;

    private boolean draggingScroll = false;
    private Button addNumericFilterButton;
    private Button addStringFilterButton;
    private Button numericChoiceButton;
    private int scrollOffset = 0;
    private int scrollRenderY;
    private List<GeneralFilterWidget> widgets = new ArrayList<>();
    private List<StatProviderAndFilterPair> filterPairs;
    private NumericType numericChoice = NumericType.SINGLE;

    public ProviderFilterListWidget(
            int x,
            int y,
            ItemFilterScreen filterScreen,
            ItemStatProvider<?> provider,
            List<StatProviderAndFilterPair> filterPairs) {
        super(x, y, 195, 145, Component.literal("Provider Filter List Widget"));

        this.filterScreen = filterScreen;
        this.provider = provider;
        this.filterPairs = new ArrayList<>(filterPairs);

        if (provider.getType().equals(String.class) && provider.getValidInputs().isEmpty()) {
            addStringFilterButton = new Button.Builder(
                            Component.translatable("screens.wynntils.itemFilter.addNewFilter"), (button -> {
                                int renderY;

                                if (widgets.isEmpty()) {
                                    renderY = getY() + 2;
                                } else {
                                    renderY = widgets.getLast().getY() + 24;
                                }

                                widgets.add(
                                        new StringFilterWidget(getX() + 5, renderY, 175, 20, null, this, filterScreen));

                                if (isScrollable()) {
                                    scroll(scrollOffset + 24);
                                }

                                addStringFilterButton.active = false;
                            }))
                    .pos(getX() + 55, getY() + 125)
                    .size(85, 20)
                    .build();
        } else if (!provider.getType().equals(Boolean.class)
                && !provider.getType().equals(String.class)) {
            numericChoiceButton = new Button.Builder(Component.literal(EnumUtils.toNiceString(numericChoice)), null)
                    .pos(getX(), getY() + 125)
                    .size(85, 20)
                    .build();

            addNumericFilterButton = new Button.Builder(
                            Component.translatable("screens.wynntils.itemFilter.addNewFilter"), (button -> {
                                int renderY;

                                if (widgets.isEmpty()) {
                                    renderY = getY() + 2;
                                } else {
                                    renderY = widgets.getLast().getY() + 24;
                                }

                                GeneralFilterWidget filterWidget = getNumericFilterWidget(renderY);

                                if (filterWidget == null) return;

                                widgets.add(filterWidget);

                                if (isScrollable()) {
                                    scroll(scrollOffset + 24);
                                }

                                addNumericFilterButton.active = false;
                            }))
                    .pos(getX() + 95, getY() + 125)
                    .size(85, 20)
                    .build();
        }

        createWidgets();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (addStringFilterButton != null) {
            addStringFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
        } else if (addNumericFilterButton != null) {
            addNumericFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
            numericChoiceButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (widgets.isEmpty()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.noFilters")),
                            getX(),
                            getX() + getWidth(),
                            getY(),
                            getY() + getScrollableArea(),
                            180,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            return;
        }

        RenderUtils.enableScissor(guiGraphics, getX(), getY() - 2, getWidth(), (int) (getScrollbarHeight() + 5));

        for (GeneralFilterWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.disableScissor(guiGraphics);

        if (isScrollable()) {
            renderScrollBar(guiGraphics);
        }

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (MathUtils.isInside(
                mouseX,
                mouseY,
                getX() + SCROLLBAR_RENDER_X,
                getX() + SCROLLBAR_RENDER_X + SCROLLBAR_WIDTH,
                scrollRenderY,
                scrollRenderY + SCROLLBAR_HEIGHT)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (addStringFilterButton != null && addStringFilterButton.isMouseOver(event.x(), event.y())) {
            return addStringFilterButton.mouseClicked(event, isDoubleClick);
        } else if (addNumericFilterButton != null && addNumericFilterButton.isMouseOver(event.x(), event.y())) {
            return addNumericFilterButton.mouseClicked(event, isDoubleClick);
        } else if (numericChoiceButton != null && numericChoiceButton.isMouseOver(event.x(), event.y())) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                cycleNumericChoice(1);
                return true;
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                cycleNumericChoice(-1);
                return true;
            }

            return false;
        }

        // Don't want to call mouse events for ones outside of the mask area
        if (event.y() > getY() + 2 && event.y() < getY() + 2 + getScrollbarHeight()) {
            for (GeneralFilterWidget widget : widgets) {
                if (widget.isMouseOver(event.x(), event.y())) {
                    return widget.mouseClicked(event, isDoubleClick);
                }
            }
        }

        if (!draggingScroll && isScrollable()) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    getX() + SCROLLBAR_RENDER_X,
                    getX() + SCROLLBAR_RENDER_X + SCROLLBAR_WIDTH,
                    scrollRenderY,
                    scrollRenderY + SCROLLBAR_HEIGHT)) {
                draggingScroll = true;

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = getY();

            int newOffset = Math.round(MathUtils.map(
                    (float) event.y(),
                    scrollAreaStartY,
                    scrollAreaStartY + getScrollbarHeight(),
                    0,
                    getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;

        if (event.y() > getY() + 2 && event.y() < getY() + 2 + getScrollbarHeight()) {
            for (GeneralFilterWidget widget : widgets) {
                if (widget.isMouseOver(event.x(), event.y())) {
                    return widget.mouseReleased(event);
                }
            }
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isScrollable()) {
            int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

            int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
            scroll(newOffset);

            return true;
        }

        return false;
    }

    public void createWidgets() {
        widgets = new ArrayList<>();

        if (!filterPairs.isEmpty()) {
            for (StatProviderAndFilterPair filterPair : filterPairs) {
                if (filterPair.statFilter() instanceof AnyStatFilters.AbstractAnyStatFilter) {
                    widgets.add(new AnyFilterWidget(getX(), getY(), this));

                    if (addStringFilterButton != null) {
                        addStringFilterButton.visible = false;
                    } else if (addNumericFilterButton != null) {
                        addNumericFilterButton.visible = false;
                        numericChoiceButton.visible = false;
                    }

                    return;
                }
            }
        }

        if (provider.getType().equals(Boolean.class)) {
            if (filterPairs.isEmpty()) {
                widgets.add(new BooleanFilterWidget(getX(), getY(), null, this));
            } else {
                widgets.add(new BooleanFilterWidget(getX(), getY(), filterPairs.getFirst(), this));
            }

            return;
        } else if (!provider.getValidInputs().isEmpty()) {
            int renderY = getY() + 2;

            for (int i = 0; i < provider.getValidInputs().size(); i++) {
                String valueName = provider.getValidInputs().get(i);

                Optional<StatProviderAndFilterPair> filterPair = filterPairs.stream()
                        .filter(filter -> filter.statFilter().asString().equalsIgnoreCase(valueName))
                        .findFirst();

                SelectionFilterWidget filterWidget =
                        new SelectionFilterWidget(getX() + 5, renderY, 150, 20, valueName, filterPair, this);

                widgets.add(filterWidget);

                renderY += 24;
            }

            scroll(scrollOffset);

            return;
        }

        int renderY = getY() + 2;
        GeneralFilterWidget filterWidget;

        for (StatProviderAndFilterPair filterPair : filterPairs) {
            if (filterPair.statProvider().getType().equals(String.class)) {
                filterWidget = new StringFilterWidget(getX() + 5, renderY, 175, 20, filterPair, this, filterScreen);
            } else {
                filterWidget = NumericFilterWidgetFactory.createFilterWidget(
                        provider.getType(), getX() + 5, renderY, 175, 20, filterPair, this, filterScreen);
            }

            if (filterWidget != null) {
                widgets.add(filterWidget);

                renderY += 24;
            }
        }

        if (provider.getType().equals(String.class)) {
            addStringFilterButton.visible = true;
            addStringFilterButton.active = true;
        } else if (!provider.getType().equals(Boolean.class)) {
            addNumericFilterButton.visible = true;
            addNumericFilterButton.active = true;
            numericChoiceButton.visible = true;
            numericChoiceButton.active = true;
        }

        scroll(scrollOffset);
    }

    public final void onFiltersChanged(List<StatProviderAndFilterPair> filterPairs) {
        this.filterPairs = new ArrayList<>(filterPairs);

        createWidgets();
    }

    public void updateQuery() {
        List<StatProviderAndFilterPair> newFilterpairs = new ArrayList<>();

        for (GeneralFilterWidget filterWidget : widgets) {
            newFilterpairs.add(filterWidget.getFilterPair());
        }

        filterPairs =
                new ArrayList<>(newFilterpairs.stream().filter(Objects::nonNull).toList());

        // Deactivate the add buttons if there were empty widgets present (from filtering out null)
        if (addStringFilterButton != null) {
            addStringFilterButton.active = filterPairs.size() == newFilterpairs.size();
        } else if (addNumericFilterButton != null) {
            addNumericFilterButton.active = filterPairs.size() == newFilterpairs.size();
        }

        filterScreen.setFiltersForProvider(provider, filterPairs);
    }

    public void removeWidget(GeneralFilterWidget filterWidget) {
        widgets.remove(filterWidget);

        scrollOffset = 0;

        updateQuery();
        createWidgets();
    }

    public ItemStatProvider<?> getProvider() {
        return provider;
    }

    private boolean isScrollable() {
        if (anyFilterActive()) {
            return false;
        } else if (provider.getValidInputs().isEmpty()) {
            return widgets.size() > MAX_WIDGETS_PER_PAGE;
        } else {
            return provider.getValidInputs().size() > MAX_SELECTION_WIDGETS_PER_PAGE;
        }
    }

    private float getScrollableArea() {
        if (provider.getValidInputs().isEmpty() && !anyFilterActive()) {
            return MAX_WIDGETS_PER_PAGE * 22 - SCROLLBAR_HEIGHT;
        } else {
            return MAX_SELECTION_WIDGETS_PER_PAGE * 22 - SCROLLBAR_HEIGHT;
        }
    }

    private int getMaxScrollOffset() {
        if (provider.getValidInputs().isEmpty() && !anyFilterActive()) {
            return (widgets.size() - MAX_WIDGETS_PER_PAGE) * 24;
        } else {
            return (provider.getValidInputs().size() - MAX_SELECTION_WIDGETS_PER_PAGE) * 24;
        }
    }

    private float getScrollbarHeight() {
        if (provider.getValidInputs().isEmpty() && !anyFilterActive()) {
            return MAX_WIDGETS_PER_PAGE * 24;
        } else {
            return MAX_SELECTION_WIDGETS_PER_PAGE * 24;
        }
    }

    private boolean anyFilterActive() {
        return !widgets.isEmpty() && widgets.getFirst() instanceof AnyFilterWidget;
    }

    private void cycleNumericChoice(int direction) {
        List<NumericType> types = new ArrayList<>(List.of(NumericType.values()));

        if (types.indexOf(numericChoice) + direction < 0) {
            numericChoice = types.getLast();
        } else if (types.indexOf(numericChoice) + direction == types.size()) {
            numericChoice = types.getFirst();
        } else {
            numericChoice = types.get(types.indexOf(numericChoice) + direction);
        }

        numericChoiceButton.setMessage(Component.literal(EnumUtils.toNiceString(numericChoice)));
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.LIGHT_GRAY,
                getX() + SCROLLBAR_RENDER_X,
                getY(),
                SCROLLBAR_WIDTH,
                getScrollbarHeight());

        scrollRenderY = (int) (getY()
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, getScrollbarHeight() - SCROLLBAR_HEIGHT));

        RenderUtils.drawRect(
                guiGraphics,
                draggingScroll ? CommonColors.BLACK : CommonColors.GRAY,
                getX() + SCROLLBAR_RENDER_X,
                scrollRenderY,
                SCROLLBAR_WIDTH,
                SCROLLBAR_HEIGHT);
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;

        for (GeneralFilterWidget filterWidget : widgets) {
            int newY = getY() + 2 + (widgets.indexOf(filterWidget) * 24) - scrollOffset;

            filterWidget.updateY(newY);
            filterWidget.visible = newY >= (getY() + 2 - 24) && newY <= (getY() + 2 + (getScrollbarHeight()));
        }
    }

    private GeneralFilterWidget getNumericFilterWidget(int renderY) {
        return switch (numericChoice) {
            case SINGLE ->
                NumericFilterWidgetFactory.createSingleWidget(
                        provider.getType(), getX() + 5, renderY, 175, 20, null, this, filterScreen);
            case RANGED ->
                NumericFilterWidgetFactory.createRangedWidget(
                        provider.getType(), getX() + 5, renderY, 175, 20, null, this, filterScreen);
            case INEQUALITY ->
                NumericFilterWidgetFactory.createInequalityWidget(
                        provider.getType(), getX() + 5, renderY, 175, 20, null, this, filterScreen);
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private enum NumericType {
        SINGLE,
        RANGED,
        INEQUALITY
    }
}

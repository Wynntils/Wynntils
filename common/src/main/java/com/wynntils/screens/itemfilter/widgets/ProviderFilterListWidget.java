/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProviderFilterListWidget extends AbstractWidget {
    private static final int MAX_WIDGETS_PER_PAGE = 5;
    private static final int MAX_SELECTION_WIDGETS_PER_PAGE = 6;
    private static final int SCROLLBAR_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_RENDER_X = 184;

    private final float translationX;
    private final float translationY;
    private final ItemFilterScreen filterScreen;
    private final ItemStatProvider<?> provider;

    private boolean draggingScroll = false;
    private Button addNumericFilterButton;
    private Button addStringFilterButton;
    private Button numericChoiceButton;
    private double currentUnusedScroll = 0;
    private int scrollOffset = 0;
    private int scrollRenderY;
    private List<GeneralFilterWidget> widgets = new ArrayList<>();
    private List<StatProviderAndFilterPair> filterPairs;
    private NumericType numericChoice = NumericType.SINGLE;

    public ProviderFilterListWidget(
            ItemFilterScreen filterScreen,
            ItemStatProvider<?> provider,
            List<StatProviderAndFilterPair> filterPairs,
            float translationX,
            float translationY) {
        super(150, 30, 195, 145, Component.literal("Provider Filter List Widget"));

        this.filterScreen = filterScreen;
        this.provider = provider;
        this.translationX = translationX;
        this.translationY = translationY;
        this.filterPairs = new ArrayList<>(filterPairs);

        if (provider.getType().equals(String.class) && provider.getValidInputs().isEmpty()) {
            addStringFilterButton = new Button.Builder(
                            Component.translatable("screens.wynntils.itemFilter.addNewFilter"), (button -> {
                                int renderY;

                                if (widgets.isEmpty()) {
                                    renderY = getY() + 2;
                                } else {
                                    renderY = widgets.get(widgets.size() - 1).getY() + 24;
                                }

                                widgets.add(
                                        new StringFilterWidget(getX() + 5, renderY, 175, 20, null, this, filterScreen));

                                scroll(1);

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
                                    renderY = widgets.get(widgets.size() - 1).getY() + 24;
                                }

                                GeneralFilterWidget filterWidget = getNumericFilterWidget(renderY);

                                if (filterWidget == null) return;

                                widgets.add(filterWidget);

                                scroll(1);

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
                            guiGraphics.pose(),
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

        for (GeneralFilterWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (isScrollable()) {
            renderScrollBar(guiGraphics.pose());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (addStringFilterButton != null && addStringFilterButton.isMouseOver(mouseX, mouseY)) {
            return addStringFilterButton.mouseClicked(mouseX, mouseY, button);
        } else if (addNumericFilterButton != null && addNumericFilterButton.isMouseOver(mouseX, mouseY)) {
            return addNumericFilterButton.mouseClicked(mouseX, mouseY, button);
        } else if (numericChoiceButton != null && numericChoiceButton.isMouseOver(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                cycleNumericChoice(1);
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                cycleNumericChoice(-1);
                return true;
            }

            return false;
        }

        for (GeneralFilterWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (!draggingScroll && isScrollable()) {
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

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int renderY = getY() + 5;
            int scrollAreaStartY = renderY + 10;

            int newValue = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + getScrollableArea(),
                    0,
                    Math.max(0, getMaxScrollOffset())));

            scroll(newValue - scrollOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        for (GeneralFilterWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseReleased(mouseX, mouseY, button);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isScrollable()) {
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

            return true;
        }

        return false;
    }

    public void createWidgets() {
        widgets = new ArrayList<>();

        scrollOffset = 0;

        if (!filterPairs.isEmpty()) {
            for (StatProviderAndFilterPair filterPair : filterPairs) {
                if (filterPair.statFilter() instanceof AnyStatFilters.AbstractAnyStatFilter) {
                    widgets.add(new AnyFilterWidget(this));

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
                widgets.add(new BooleanFilterWidget(null, this));
            } else {
                widgets.add(new BooleanFilterWidget(filterPairs.get(0), this));
            }

            return;
        } else if (!provider.getValidInputs().isEmpty()) {
            int renderY = getY() + 2 - (24 * scrollOffset);

            for (int i = 0; i < provider.getValidInputs().size(); i++) {
                String valueName = provider.getValidInputs().get(i);

                Optional<StatProviderAndFilterPair> filterPair = filterPairs.stream()
                        .filter(filter -> filter.statFilter().asString().equalsIgnoreCase(valueName))
                        .findFirst();

                SelectionFilterWidget filterWidget =
                        new SelectionFilterWidget(getX() + 5, renderY, 150, 20, valueName, filterPair, this);

                if (renderY < getY() || renderY > getY() + getScrollbarHeight()) {
                    filterWidget.visible = false;
                }

                widgets.add(filterWidget);

                renderY += 24;
            }

            return;
        }

        int renderY = getY() + 2 - (24 * scrollOffset);
        GeneralFilterWidget filterWidget;

        for (StatProviderAndFilterPair filterPair : filterPairs) {
            if (filterPair.statProvider().getType().equals(String.class)) {
                filterWidget = new StringFilterWidget(getX() + 5, renderY, 175, 20, filterPair, this, filterScreen);
            } else {
                filterWidget = NumericFilterWidgetFactory.createFilterWidget(
                        provider.getType(), getX() + 5, renderY, 175, 20, filterPair, this, filterScreen);
            }

            if (filterWidget != null) {
                if (renderY < getY() || renderY > getY() + getScrollbarHeight()) {
                    filterWidget.visible = false;
                }

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

        // Deactive the add buttons if there were empty widgets present (from filtering out null)
        if (addStringFilterButton != null) {
            addStringFilterButton.active = filterPairs.size() == newFilterpairs.size();
        } else if (addNumericFilterButton != null) {
            addNumericFilterButton.active = filterPairs.size() == newFilterpairs.size();
        }

        filterScreen.setFiltersForProvider(provider, filterPairs);
    }

    public void removeWidget(GeneralFilterWidget filterWidget) {
        widgets.remove(filterWidget);

        updateQuery();
        createWidgets();
    }

    public ItemStatProvider<?> getProvider() {
        return provider;
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    private boolean isScrollable() {
        if (provider.getValidInputs().isEmpty()) {
            return widgets.size() > MAX_WIDGETS_PER_PAGE;
        } else {
            return widgets.size() > MAX_SELECTION_WIDGETS_PER_PAGE;
        }
    }

    private float getScrollableArea() {
        if (provider.getValidInputs().isEmpty()) {
            return MAX_WIDGETS_PER_PAGE * 22 - SCROLLBAR_HEIGHT;
        } else {
            return MAX_SELECTION_WIDGETS_PER_PAGE * 22 - SCROLLBAR_HEIGHT;
        }
    }

    private float getMaxScrollOffset() {
        if (provider.getValidInputs().isEmpty()) {
            return widgets.size() - MAX_WIDGETS_PER_PAGE;
        } else {
            return widgets.size() - MAX_SELECTION_WIDGETS_PER_PAGE;
        }
    }

    private float getScrollbarHeight() {
        if (provider.getValidInputs().isEmpty()) {
            return MAX_WIDGETS_PER_PAGE * 24;
        } else {
            return MAX_SELECTION_WIDGETS_PER_PAGE * 24;
        }
    }

    private void cycleNumericChoice(int direction) {
        List<NumericType> types = new ArrayList<>(List.of(NumericType.values()));

        if (types.indexOf(numericChoice) + direction < 0) {
            numericChoice = types.get(types.size() - 1);
        } else if (types.indexOf(numericChoice) + direction == types.size()) {
            numericChoice = types.get(0);
        } else {
            numericChoice = types.get(types.indexOf(numericChoice) + direction);
        }

        numericChoiceButton.setMessage(Component.literal(EnumUtils.toNiceString(numericChoice)));
    }

    private void renderScrollBar(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack,
                CommonColors.LIGHT_GRAY,
                getX() + SCROLLBAR_RENDER_X,
                getY(),
                0,
                SCROLLBAR_WIDTH,
                getScrollbarHeight());

        scrollRenderY = (int) (getY()
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, getScrollbarHeight() - SCROLLBAR_HEIGHT));

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
        // Prevent scrolling too far
        if (scrollOffset == (int) getMaxScrollOffset() && delta == 1) return;
        if (scrollOffset == 0 && delta == -1) return;
        if (getMaxScrollOffset() < 0) return;

        scrollOffset = (int) MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, getMaxScrollOffset()));

        for (GeneralFilterWidget filterWidget : widgets) {
            int newY;

            if (delta == -1) {
                newY = filterWidget.getY() + 24;
            } else if (delta == 1) {
                newY = filterWidget.getY() - 24;
            } else {
                return;
            }

            filterWidget.updateY(newY);

            filterWidget.visible = newY >= getY() && !(newY > getY() + getScrollbarHeight());
        }
    }

    private GeneralFilterWidget getNumericFilterWidget(int renderY) {
        return switch (numericChoice) {
            case SINGLE -> NumericFilterWidgetFactory.createSingleWidget(
                    provider.getType(), getX() + 5, renderY, 175, 20, null, this, filterScreen);
            case RANGED -> NumericFilterWidgetFactory.createRangedWidget(
                    provider.getType(), getX() + 5, renderY, 175, 20, null, this, filterScreen);
            case INEQUALITY -> NumericFilterWidgetFactory.createInequalityWidget(
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

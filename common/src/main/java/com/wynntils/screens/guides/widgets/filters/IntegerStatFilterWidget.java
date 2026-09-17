/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class IntegerStatFilterWidget<T extends ItemStatProvider<?>> extends GuideFilterWidget {
    private static final int COLLAPSED_HEIGHT = 30;
    private static final int ENABLED_HEIGHT = 54;
    private static final int CUSTOM_HEIGHT = 104;
    private static final int SLIDER_HEIGHT = 20;

    private final int minValue;
    private final int maxValue;

    private final Function<String, Optional<? extends StatFilter<?>>> rangedFilterFactory;
    private final Supplier<? extends StatFilter<?>> anyFilterSupplier;
    protected T provider;

    private GuideFilterCheckbox<T> enabledCheckbox;
    private ModeButton modeButton;
    private StatSlider minSlider;
    private StatSlider maxSlider;
    private FilterMode filterMode = FilterMode.ANY;

    public static <U extends ItemStatProvider<?>> IntegerStatFilterWidget<U> create(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, U provider) {
        RangedValue expectedRange = getExpectedRange(provider);

        return new IntegerStatFilterWidget<>(
                containerWidget, searchQuery, provider, expectedRange.low(), expectedRange.high());
    }

    public static <U extends ItemStatProvider<?>> IntegerStatFilterWidget<U> createStatValue(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, U provider) {
        RangedValue expectedRange = getExpectedRange(provider);

        return new IntegerStatFilterWidget<>(
                containerWidget,
                searchQuery,
                provider,
                expectedRange.low(),
                expectedRange.high(),
                statValueRangeFilterFactory(),
                statValueAnyFilterSupplier());
    }

    public IntegerStatFilterWidget(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, int minValue, int maxValue) {
        this(
                containerWidget,
                searchQuery,
                null,
                minValue,
                maxValue,
                integerRangeFilterFactory(),
                AnyStatFilters.AnyIntegerStatFilter::new);
    }

    public IntegerStatFilterWidget(
            GuideContainerWidget<?> containerWidget,
            ItemSearchQuery searchQuery,
            T provider,
            int minValue,
            int maxValue) {
        this(
                containerWidget,
                searchQuery,
                provider,
                minValue,
                maxValue,
                integerRangeFilterFactory(),
                AnyStatFilters.AnyIntegerStatFilter::new);
    }

    private IntegerStatFilterWidget(
            GuideContainerWidget<?> containerWidget,
            ItemSearchQuery searchQuery,
            T provider,
            int minValue,
            int maxValue,
            Function<String, Optional<? extends StatFilter<?>>> rangedFilterFactory,
            Supplier<? extends StatFilter<?>> anyFilterSupplier) {
        super(COLLAPSED_HEIGHT, containerWidget);

        this.provider = provider;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.rangedFilterFactory = rangedFilterFactory;
        this.anyFilterSupplier = anyFilterSupplier;

        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(provider.getDisplayName())
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        getWidth(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        enabledCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!enabledCheckbox.selected) return;

        modeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (filterMode == FilterMode.CUSTOM) {
            minSlider.render(guiGraphics, mouseX, mouseY, partialTick);
            maxSlider.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        if (enabledCheckbox.isMouseOver(event.x(), event.y())) {
            clicked = enabledCheckbox.mouseClicked(event, isDoubleClick);
        } else if (enabledCheckbox.selected && modeButton.isMouseOver(event.x(), event.y())) {
            clicked = modeButton.mouseClicked(event, isDoubleClick);
        } else if (enabledCheckbox.selected && filterMode == FilterMode.CUSTOM) {
            if (minSlider.isMouseOver(event.x(), event.y())) {
                clicked = minSlider.mouseClicked(event, isDoubleClick);
            } else if (maxSlider.isMouseOver(event.x(), event.y())) {
                clicked = maxSlider.mouseClicked(event, isDoubleClick);
            }
        }

        if (clicked) {
            updateHeight();
            containerWidget.updateSearchFromQuickFilters();
        }
        return clicked;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (filterMode == FilterMode.CUSTOM) {
            if (minSlider.dragging) {
                minSlider.mouseDragged(event, dragX, dragY);
                if (minSlider.getValue() > maxSlider.getValue()) maxSlider.setStatValue(minSlider.getValue());
            } else if (maxSlider.dragging) {
                maxSlider.mouseDragged(event, dragX, dragY);
                if (maxSlider.getValue() < minSlider.getValue()) minSlider.setStatValue(maxSlider.getValue());
            }
            containerWidget.updateSearchFromQuickFilters();
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        minSlider.dragging = false;
        maxSlider.dragging = false;
        return false;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        enabledCheckbox = new GuideFilterCheckbox<>("Enabled") {
            @Override
            protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
                selected = hasProviderFilter(searchQuery);
            }

            @Override
            protected StatProviderAndFilterPair getFilterPair(T provider) {
                return null;
            }
        };
        modeButton = new ModeButton();
        minSlider = new StatSlider(true, minValue, minValue, maxValue);
        maxSlider = new StatSlider(false, maxValue, minValue, maxValue);
        updateFromQuery(searchQuery);
        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (enabledCheckbox == null) return;

        enabledCheckbox.setPosition(getX(), getY() + 10);
        modeButton.setPosition(getX(), getY() + 32);
        minSlider.setPosition(getX(), getY() + 56);
        maxSlider.setPosition(getX(), getY() + 78);
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        if (!enabledCheckbox.selected) return List.of();

        Optional<? extends StatFilter<?>> statFilter =
                switch (filterMode) {
                    case ANY -> Optional.of(anyFilterSupplier.get());
                    case POSITIVE -> rangedFilterFactory.apply(">=1");
                    case NEGATIVE -> rangedFilterFactory.apply("<=-1");
                    case CUSTOM -> rangedFilterFactory.apply(minSlider.getValue() + "-" + maxSlider.getValue());
                };

        return statFilter
                .map(filter -> List.of(new StatProviderAndFilterPair(provider, filter)))
                .orElse(List.of());
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        return provider;
    }

    public static Function<String, Optional<? extends StatFilter<?>>> integerRangeFilterFactory() {
        return input -> new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory().create(input);
    }

    public static Function<String, Optional<? extends StatFilter<?>>> statValueRangeFilterFactory() {
        return input ->
                new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory().create(input);
    }

    public static Supplier<? extends StatFilter<?>> statValueAnyFilterSupplier() {
        return AnyStatFilters.AnyStatValueStatFilter::new;
    }

    private static RangedValue getExpectedRange(ItemStatProvider<?> provider) {
        return provider.getExpectedRange()
                .orElseThrow(() ->
                        new IllegalStateException("Could not get expected range for " + provider.getDisplayName()));
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        enabledCheckbox.updateStateFromQuery(searchQuery);

        List<StatProviderAndFilterPair> filters = getProviderFilters(searchQuery);
        if (filters.isEmpty()) {
            filterMode = FilterMode.ANY;
            minSlider.setStatValue(minValue);
            maxSlider.setStatValue(maxValue);
        } else if (filters.getFirst().statFilter() instanceof AnyStatFilters.AbstractAnyStatFilter<?>) {
            filterMode = FilterMode.ANY;
        } else if (filters.getFirst().statFilter()
                instanceof RangedStatFilters.AbstractRangedStatFilter<?> rangedFilter) {
            if (rangedFilter.getMin() == 1 && rangedFilter.getMax() == Integer.MAX_VALUE) {
                filterMode = FilterMode.POSITIVE;
            } else if (rangedFilter.getMin() == Integer.MIN_VALUE && rangedFilter.getMax() == -1) {
                filterMode = FilterMode.NEGATIVE;
            } else {
                filterMode = FilterMode.CUSTOM;
                minSlider.setStatValue(MathUtils.clamp(rangedFilter.getMin(), minValue, maxValue));
                maxSlider.setStatValue(MathUtils.clamp(rangedFilter.getMax(), minValue, maxValue));
            }
        }

        modeButton.updateMessage();
        updateHeight();
    }

    private void updateHeight() {
        setHeight(
                !enabledCheckbox.selected
                        ? COLLAPSED_HEIGHT
                        : filterMode == FilterMode.CUSTOM ? CUSTOM_HEIGHT : ENABLED_HEIGHT);
    }

    private boolean hasProviderFilter(ItemSearchQuery searchQuery) {
        return !getProviderFilters(searchQuery).isEmpty();
    }

    private List<StatProviderAndFilterPair> getProviderFilters(ItemSearchQuery searchQuery) {
        return searchQuery.filters().values().stream()
                .filter(filterPair -> filterPair.statProvider().getName().equals(provider.getName()))
                .toList();
    }

    private double valueFor(int statValue) {
        if (minValue == maxValue) return 0d;

        return (double) (MathUtils.clamp(statValue, minValue, maxValue) - minValue) / (maxValue - minValue);
    }

    private int valueToStatValue(double sliderValue) {
        if (minValue == maxValue) return minValue;

        return (int) Math.round(minValue + sliderValue * (maxValue - minValue));
    }

    private enum FilterMode {
        ANY("Any"),
        POSITIVE("Positive"),
        NEGATIVE("Negative"),
        CUSTOM("Custom");

        private final String label;

        FilterMode(String label) {
            this.label = label;
        }
    }

    private class ModeButton extends AbstractWidget {
        private ModeButton() {
            super(0, 0, 128, 20, Component.empty());
            updateMessage();
        }

        @Override
        protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawRect(
                    guiGraphics,
                    (isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY).withAlpha(0.5f),
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight());

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(getMessage()),
                            getX() + 4,
                            getY() + 10,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (isHovered) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() == 0) {
                filterMode = nextMode(filterMode);
            } else if (event.button() == 1) {
                filterMode = previousMode(filterMode);
            }

            updateMessage();
            updateHeight();
            return true;
        }

        private void updateMessage() {
            setMessage(Component.literal("Value: " + filterMode.label)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        }

        private FilterMode nextMode(FilterMode mode) {
            do {
                mode = FilterMode.values()[(mode.ordinal() + 1) % FilterMode.values().length];
            } while (mode == FilterMode.NEGATIVE && minValue >= 0);

            return mode;
        }

        private FilterMode previousMode(FilterMode mode) {
            do {
                mode = FilterMode.values()[
                        (mode.ordinal() - 1 + FilterMode.values().length) % FilterMode.values().length];
            } while (mode == FilterMode.NEGATIVE && minValue >= 0);

            return mode;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private class StatSlider extends GuideFilterSlider<T> {
        private final boolean minSlider;
        private int statValue;

        private StatSlider(boolean minSlider, int initialValue, int minValue, int maxValue) {
            super(0, 0, 128, SLIDER_HEIGHT, Component.empty(), valueFor(initialValue));
            this.minSlider = minSlider;
            this.statValue = initialValue;
            updateMessage();
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {}

        private int getValue() {
            return statValue;
        }

        private void setStatValue(int statValue) {
            this.statValue = MathUtils.clamp(statValue, minValue, maxValue);
            this.value = valueFor(this.statValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal((minSlider ? "Min: " : "Max: ") + statValue)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        }

        @Override
        protected void applyValue() {
            statValue = valueToStatValue(value);
        }
    }
}

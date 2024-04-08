/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.PercentageStatFilter;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class StatValueValueWidget extends NumericValueWidget {
    private WynntilsCheckbox singlePercentageCheckbox;
    private WynntilsCheckbox rangedPercentageCheckbox;
    private WynntilsCheckbox greaterThanPercentageCheckbox;
    private WynntilsCheckbox lessThanPercentageCheckbox;

    public StatValueValueWidget(
            List<StatProviderAndFilterPair> filters,
            ItemStatProvider<?> itemStatProvider,
            ItemFilterScreen filterScreen) {
        super(filters, itemStatProvider, filterScreen);

        singlePercentageCheckbox = new WynntilsCheckbox(
                getX() + 165,
                getY() + 12,
                20,
                20,
                Component.literal("%"),
                false,
                10,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));

        rangedPercentageCheckbox = new WynntilsCheckbox(
                getX() + 165,
                getY() + 48,
                20,
                20,
                Component.literal("%"),
                false,
                10,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));

        greaterThanPercentageCheckbox = new WynntilsCheckbox(
                getX() + 165,
                getY() + 82,
                20,
                20,
                Component.literal("%"),
                false,
                10,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));

        lessThanPercentageCheckbox = new WynntilsCheckbox(
                getX() + 165,
                getY() + 116,
                20,
                20,
                Component.literal("%"),
                false,
                10,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));

        widgets.add(singlePercentageCheckbox);
        widgets.add(rangedPercentageCheckbox);
        widgets.add(greaterThanPercentageCheckbox);
        widgets.add(lessThanPercentageCheckbox);
    }

    @Override
    public void onFiltersChanged(List<StatProviderAndFilterPair> filters) {
        // FIXME: This ValueWidget can only handle a single filter of a type at a time for the current provider
        //        (for example, only one single value filter, one ranged value filter, etc.,
        //        while the ItemFilterService supports multiple filters of the same type)
        // FIXME: Additionally, this widget
        super.onFiltersChanged(filters);

        greaterThanPercentageCheckbox.selected = false;
        lessThanPercentageCheckbox.selected = false;

        for (StatProviderAndFilterPair filterPair : filters) {
            StatFilter filter = filterPair.statFilter();

            // Other filter types are handled by the superclass
            if (filter instanceof PercentageStatFilter percentageFilter) {
                // Single value, if min and max are equal
                if (percentageFilter.getMin() == percentageFilter.getMax()) {
                    singleInput.setTextBoxInput(String.valueOf(percentageFilter.getMin()));
                    continue;
                }

                // Greater than (or equals) value if min is not the minimum value, and max is the maximum value
                if (percentageFilter.getMin() != Float.MIN_VALUE && percentageFilter.getMax() == Float.MAX_VALUE) {
                    greaterThanInput.setTextBoxInput(String.valueOf(percentageFilter.getMin()));
                    greaterThanEqual = percentageFilter.isEqualsInString();
                    greaterThanPercentageCheckbox.selected = true;
                    continue;
                }

                // Less than (or equals) value if max is not the maximum value, and min is the minimum value
                if (percentageFilter.getMax() != Float.MAX_VALUE && percentageFilter.getMin() == Float.MIN_VALUE) {
                    lessThanInput.setTextBoxInput(String.valueOf(percentageFilter.getMax()));
                    lessThanEqual = percentageFilter.isEqualsInString();
                    lessThanPercentageCheckbox.selected = true;
                    continue;
                }

                // Ranged value, any other case
                rangedMinInput.setTextBoxInput(String.valueOf(percentageFilter.getMin()));
                rangedMaxInput.setTextBoxInput(String.valueOf(percentageFilter.getMax()));
            }
        }
    }

    @Override
    protected StatFilter getAnyStatFilter() {
        return new AnyStatFilters.AnyStatValueStatFilter();
    }

    @Override
    protected Optional<StatFilter> getSingleStatFilter(String value) {
        if (singlePercentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create(value + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create(value)
                    .map(f -> f);
        }
    }

    @Override
    protected Optional<StatFilter> getRangedStatFilter(String min, String max) {
        if (rangedPercentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create(min + "-" + max + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create(min + "-" + max)
                    .map(f -> f);
        }
    }

    @Override
    protected Optional<StatFilter> getGreaterThanStatFilter(String value, boolean equal) {
        if (greaterThanPercentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create((equal ? ">=" : ">") + value + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create((equal ? ">=" : ">") + value)
                    .map(f -> f);
        }
    }

    @Override
    protected Optional<StatFilter> getLessThanStatFilter(String value, boolean equal) {
        if (lessThanPercentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create((equal ? "<=" : "<") + value + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create((equal ? "<=" : "<") + value)
                    .map(f -> f);
        }
    }
}

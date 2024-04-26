/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.type;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.GeneralFilterWidget;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.InequalityCappedValueFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.InequalityIntegerFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.InequalityStatValueFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.RangedCappedValueFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.RangedIntegerFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.RangedStatValueFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.SingleCappedValueFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.SingleIntegerFilterWidget;
import com.wynntils.screens.itemfilter.widgets.numeric.SingleStatValueFilterWidget;
import com.wynntils.services.itemfilter.filters.PercentageStatFilter;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.CappedValue;
import java.util.Map;
import java.util.function.Function;

public final class NumericFilterWidgetFactory {
    private static final Map<Class<?>, Function<WidgetParams, GeneralFilterWidget>> singleWidgetMap = Map.of(
            Integer.class,
            params -> new SingleIntegerFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            CappedValue.class,
            params -> new SingleCappedValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            StatValue.class,
            params -> new SingleStatValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()));
    private static final Map<Class<?>, Function<WidgetParams, GeneralFilterWidget>> rangedWidgetMap = Map.of(
            Integer.class,
            params -> new RangedIntegerFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            CappedValue.class,
            params -> new RangedCappedValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            StatValue.class,
            params -> new RangedStatValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()));
    private static final Map<Class<?>, Function<WidgetParams, GeneralFilterWidget>> inequalityWidgetMap = Map.of(
            Integer.class,
            params -> new InequalityIntegerFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            CappedValue.class,
            params -> new InequalityCappedValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()),
            StatValue.class,
            params -> new InequalityStatValueFilterWidget(
                    params.x(),
                    params.y(),
                    params.width(),
                    params.height(),
                    params.filterPair(),
                    params.parent(),
                    params.filterScreen()));

    public static GeneralFilterWidget createFilterWidget(
            Class<?> type,
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        if (type.equals(Integer.class)) {
            if (filterPair.statFilter() instanceof RangedStatFilters.RangedIntegerStatFilter rangedFilter) {
                if (rangedFilter.getMin() == rangedFilter.getMax()) {
                    return new SingleIntegerFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else if ((rangedFilter.getMin() != Integer.MIN_VALUE && rangedFilter.getMax() == Integer.MAX_VALUE)
                        || rangedFilter.getMax() != Integer.MAX_VALUE && rangedFilter.getMin() == Integer.MIN_VALUE) {
                    return new InequalityIntegerFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else {
                    return new RangedIntegerFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                }
            }
        }

        if (type.equals(CappedValue.class)) {
            if (filterPair.statFilter() instanceof RangedStatFilters.RangedCappedValueStatFilter rangedFilter) {
                if (rangedFilter.getMin() == rangedFilter.getMax()) {
                    return new SingleCappedValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else if ((rangedFilter.getMin() != Integer.MIN_VALUE && rangedFilter.getMax() == Integer.MAX_VALUE)
                        || rangedFilter.getMax() != Integer.MAX_VALUE && rangedFilter.getMin() == Integer.MIN_VALUE) {
                    return new InequalityCappedValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else {
                    return new RangedCappedValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                }
            }
        }

        if (type.equals(StatValue.class)) {
            if (filterPair.statFilter() instanceof RangedStatFilters.RangedStatValueStatFilter rangedFilter) {
                if (rangedFilter.getMin() == rangedFilter.getMax()) {
                    return new SingleStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else if ((rangedFilter.getMin() != Integer.MIN_VALUE && rangedFilter.getMax() == Integer.MAX_VALUE)
                        || rangedFilter.getMax() != Integer.MAX_VALUE && rangedFilter.getMin() == Integer.MIN_VALUE) {
                    return new InequalityStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else {
                    return new RangedStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                }
            }

            if (filterPair.statFilter() instanceof PercentageStatFilter percentageFilter) {
                if (percentageFilter.getMin() == percentageFilter.getMax()) {
                    return new SingleStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else if ((percentageFilter.getMin() != Float.MIN_VALUE
                                && percentageFilter.getMax() == Float.MAX_VALUE)
                        || percentageFilter.getMax() != Float.MAX_VALUE
                                && percentageFilter.getMin() == Float.MIN_VALUE) {
                    return new InequalityStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                } else {
                    return new RangedStatValueFilterWidget(x, y, width, height, filterPair, parent, filterScreen);
                }
            }
        }

        return null;
    }

    public static GeneralFilterWidget createSingleWidget(
            Class<?> type,
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        WidgetParams params = new WidgetParams(x, y, width, height, filterPair, parent, filterScreen);

        Function<WidgetParams, GeneralFilterWidget> widgetFunction = singleWidgetMap.get(type);

        if (widgetFunction != null) {
            return widgetFunction.apply(params);
        } else {
            return null;
        }
    }

    public static GeneralFilterWidget createRangedWidget(
            Class<?> type,
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        WidgetParams params = new WidgetParams(x, y, width, height, filterPair, parent, filterScreen);

        Function<WidgetParams, GeneralFilterWidget> widgetFunction = rangedWidgetMap.get(type);

        if (widgetFunction != null) {
            return widgetFunction.apply(params);
        } else {
            return null;
        }
    }

    public static GeneralFilterWidget createInequalityWidget(
            Class<?> type,
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        WidgetParams params = new WidgetParams(x, y, width, height, filterPair, parent, filterScreen);

        Function<WidgetParams, GeneralFilterWidget> widgetFunction = inequalityWidgetMap.get(type);

        if (widgetFunction != null) {
            return widgetFunction.apply(params);
        } else {
            return null;
        }
    }
}

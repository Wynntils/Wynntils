/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets.numeric;

import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.services.itemfilter.filters.PercentageStatFilter;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.services.itemfilter.type.StatValue;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class InequalityStatValueFilterWidget extends InequalityNumericFilterWidget<StatValue> {
    private final WynntilsCheckbox percentageCheckbox;

    public InequalityStatValueFilterWidget(
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        super(x, y, width, height, parent, filterScreen);

        boolean percentage = false;

        if (filterPair != null && filterPair.statFilter() instanceof PercentageStatFilter percentageStatFilter) {
            percentage = true;

            if (percentageStatFilter.getMin() != Float.MIN_VALUE && percentageStatFilter.getMax() == Float.MAX_VALUE) {
                if (percentageStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.GREATER_THAN_EQUAL);
                    setEntryInput(String.valueOf(percentageStatFilter.getMin()));
                } else {
                    // When equality is not enabled, we need to subtract 1 from the value,
                    // as getMin() returns the lowest included value
                    setInequalityType(InequalityType.GREATER_THAN);
                    setEntryInput((String.valueOf(percentageStatFilter.getMin() - 1)));
                }
            } else if (percentageStatFilter.getMax() != Float.MAX_VALUE
                    && percentageStatFilter.getMin() == Float.MIN_VALUE) {
                if (percentageStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.LESS_THAN_EQUAL);
                    setEntryInput(String.valueOf(percentageStatFilter.getMax()));
                } else {
                    // When equality is not enabled, we need to add 1 to the value,
                    // as getMax() returns the highest included value
                    setInequalityType(InequalityType.LESS_THAN);
                    setEntryInput(String.valueOf(percentageStatFilter.getMax() + 1));
                }
            }
        } else if (filterPair != null
                && filterPair.statFilter() instanceof RangedStatFilters.RangedStatValueStatFilter statValueStatFilter) {
            if (statValueStatFilter.getMin() != Integer.MIN_VALUE
                    && statValueStatFilter.getMax() == Integer.MAX_VALUE) {
                if (statValueStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.GREATER_THAN_EQUAL);
                    setEntryInput(String.valueOf(statValueStatFilter.getMin()));
                } else {
                    // When equality is not enabled, we need to subtract 1 from the value,
                    // as getMin() returns the lowest included value
                    setInequalityType(InequalityType.GREATER_THAN);
                    setEntryInput((String.valueOf(statValueStatFilter.getMin() - 1)));
                }
            } else if (statValueStatFilter.getMax() != Integer.MAX_VALUE
                    && statValueStatFilter.getMin() == Integer.MIN_VALUE) {
                if (statValueStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.LESS_THAN_EQUAL);
                    setEntryInput(String.valueOf(statValueStatFilter.getMax()));
                } else {
                    // When equality is not enabled, we need to add 1 to the value,
                    // as getMax() returns the highest included value
                    setInequalityType(InequalityType.LESS_THAN);
                    setEntryInput(String.valueOf(statValueStatFilter.getMax() + 1));
                }
            }
        }

        this.percentageCheckbox = new WynntilsCheckbox(
                getX() + getWidth() - 52,
                getY(),
                20,
                Component.literal("%"),
                percentage,
                10,
                (checkbox, button) -> parent.updateQuery(),
                List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        percentageCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (percentageCheckbox.isMouseOver(mouseX, mouseY)) {
            return percentageCheckbox.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);

        percentageCheckbox.setY(y);
    }

    @Override
    protected Optional<StatFilter<StatValue>> getInequalityStatFilter(String value, InequalityType inequalityType) {
        if (percentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create(inequalityType.getMessage() + value + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create(inequalityType.getMessage() + value)
                    .map(f -> f);
        }
    }
}

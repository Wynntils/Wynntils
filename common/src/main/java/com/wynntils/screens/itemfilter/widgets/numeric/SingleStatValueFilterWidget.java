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

public class SingleStatValueFilterWidget extends SingleNumericFilterWidget<StatValue> {
    private final WynntilsCheckbox percentageCheckbox;

    public SingleStatValueFilterWidget(
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
            setEntryInput(String.valueOf(percentageStatFilter.getMin()));

            percentage = true;
        } else if (filterPair != null
                && filterPair.statFilter() instanceof RangedStatFilters.RangedStatValueStatFilter statValueStatFilter) {
            setEntryInput(statValueStatFilter.asString());
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
    protected Optional<StatFilter<StatValue>> getSingleStatFilter(String value) {
        if (percentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create(value + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create(value)
                    .map(f -> f);
        }
    }
}

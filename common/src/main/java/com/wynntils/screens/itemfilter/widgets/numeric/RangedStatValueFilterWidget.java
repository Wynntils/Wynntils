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

public class RangedStatValueFilterWidget extends RangedNumericFilterWidget<StatValue> {
    private final WynntilsCheckbox percentageCheckbox;

    public RangedStatValueFilterWidget(
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
            setMinInput(String.valueOf(percentageStatFilter.getMin()));
            setMaxInput(String.valueOf(percentageStatFilter.getMax()));

            percentage = true;
        } else if (filterPair != null
                && filterPair.statFilter() instanceof RangedStatFilters.RangedStatValueStatFilter statValueStatFilter) {
            setMinInput(String.valueOf(statValueStatFilter.getMin()));
            setMaxInput(String.valueOf(statValueStatFilter.getMax()));
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
    protected Optional<StatFilter<StatValue>> getRangedStatFilter(String min, String max) {
        if (percentageCheckbox.selected) {
            return new PercentageStatFilter.PercentageStatFilterFactory()
                    .create(min + "-" + max + "%")
                    .map(f -> f);
        } else {
            return new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory()
                    .create(min + "-" + max)
                    .map(f -> f);
        }
    }
}

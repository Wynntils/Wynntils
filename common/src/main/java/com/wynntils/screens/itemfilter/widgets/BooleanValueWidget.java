/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.BooleanStatFilter;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.type.ConfirmedBoolean;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BooleanValueWidget extends GeneralValueWidget {
    private final WynntilsCheckbox trueCheckbox;
    private final WynntilsCheckbox falseCheckbox;

    private ConfirmedBoolean state = ConfirmedBoolean.UNCONFIRMED;

    public BooleanValueWidget(ItemStatProvider<?> itemStatProvider, ItemFilterScreen filterScreen) {
        super(Component.literal("Boolean Value Widget"), itemStatProvider, filterScreen);

        this.trueCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 35,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.booleanTrue"),
                this.state == ConfirmedBoolean.TRUE,
                150,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        toggleState(ConfirmedBoolean.TRUE);
                    }
                });

        this.falseCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 90,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.booleanFalse"),
                this.state == ConfirmedBoolean.FALSE,
                150,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        toggleState(ConfirmedBoolean.FALSE);
                    }
                });

        widgets.add(this.trueCheckbox);
        widgets.add(this.falseCheckbox);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void toggleState(ConfirmedBoolean newState) {
        // Update the state and disable the opposite checkbox if necessary
        if (state == newState) {
            state = ConfirmedBoolean.UNCONFIRMED;
        } else if (newState == ConfirmedBoolean.TRUE) {
            state = ConfirmedBoolean.TRUE;
            falseCheckbox.selected = false;
        } else {
            state = ConfirmedBoolean.FALSE;
            trueCheckbox.selected = false;
        }

        updateQuery();
    }

    @Override
    public void onFiltersChanged(List<StatProviderAndFilterPair> filters) {
        // Default values
        state = ConfirmedBoolean.UNCONFIRMED;
        trueCheckbox.selected = false;
        falseCheckbox.selected = false;

        // If there are no filters, return
        if (filters == null || filters.isEmpty()) return;

        // Otherwise, get the first filter (and ignore the rest)
        StatProviderAndFilterPair filter = filters.get(0);

        // Update the state according to the filter
        if (filter.statFilter().matches(List.of(true))) {
            state = ConfirmedBoolean.TRUE;
            trueCheckbox.selected = true;
            falseCheckbox.selected = false;
        } else if (filter.statFilter().matches(List.of(false))) {
            state = ConfirmedBoolean.FALSE;
            trueCheckbox.selected = false;
            falseCheckbox.selected = true;
        }
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilterPairs() {
        if (state == ConfirmedBoolean.UNCONFIRMED) {
            return List.of();
        }

        BooleanStatFilter statFilter =
                new BooleanStatFilter.BooleanStatFilterFactory().fromBoolean(state == ConfirmedBoolean.TRUE);

        return List.of(new StatProviderAndFilterPair(itemStatProvider, statFilter));
    }
}

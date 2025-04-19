/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.services.itemfilter.filters.BooleanStatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.type.ConfirmedBoolean;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class BooleanFilterWidget extends GeneralFilterWidget {
    private final WynntilsCheckbox trueCheckbox;
    private final WynntilsCheckbox falseCheckbox;

    private ConfirmedBoolean state = ConfirmedBoolean.UNCONFIRMED;

    protected BooleanFilterWidget(int x, int y, StatProviderAndFilterPair filterPair, ProviderFilterListWidget parent) {
        super(x, y, 195, 145, Component.literal("Boolean Filter Widget"), parent);

        this.trueCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 35,
                20,
                Component.translatable("screens.wynntils.itemFilter.booleanTrue"),
                this.state == ConfirmedBoolean.TRUE,
                150,
                (checkbox, b) -> {
                    if (b) {
                        toggleState(ConfirmedBoolean.TRUE);
                    }
                });

        this.falseCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 90,
                20,
                Component.translatable("screens.wynntils.itemFilter.booleanFalse"),
                this.state == ConfirmedBoolean.FALSE,
                150,
                (checkbox, b) -> {
                    if (b) {
                        toggleState(ConfirmedBoolean.FALSE);
                    }
                });

        if (filterPair != null) {
            if (filterPair.statFilter().matches(true)) {
                state = ConfirmedBoolean.TRUE;
                trueCheckbox.selected = true;
                falseCheckbox.selected = false;
            } else if (filterPair.statFilter().matches(false)) {
                state = ConfirmedBoolean.FALSE;
                trueCheckbox.selected = false;
                falseCheckbox.selected = true;
            }
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        trueCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
        falseCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (trueCheckbox.isMouseOver(mouseX, mouseY)) {
            return trueCheckbox.mouseClicked(mouseX, mouseY, button);
        } else if (falseCheckbox.isMouseOver(mouseX, mouseY)) {
            return falseCheckbox.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        // Isn't scrollable
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        if (state == ConfirmedBoolean.UNCONFIRMED) return null;

        BooleanStatFilter statFilter =
                new BooleanStatFilter.BooleanStatFilterFactory().fromBoolean(state == ConfirmedBoolean.TRUE);

        return new StatProviderAndFilterPair(parent.getProvider(), statFilter);
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

        parent.updateQuery();
    }
}

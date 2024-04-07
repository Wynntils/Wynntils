/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.utils.type.ConfirmedBoolean;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BooleanValueWidget extends GeneralValueWidget {
    private final WynntilsCheckbox trueCheckbox;
    private final WynntilsCheckbox falseCheckbox;

    private ConfirmedBoolean state = ConfirmedBoolean.UNCONFIRMED;

    public BooleanValueWidget(String query, ItemFilterScreen filterScreen) {
        super(Component.literal("Boolean Value Widget"), filterScreen);

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

        updateValues(List.of(query));

        updateQuery();
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
    public void updateValues(List<String> query) {
        // Update the checkboxes from the searchWidget query for the current provider.
        if (query.get(0).equalsIgnoreCase("true")) {
            state = ConfirmedBoolean.TRUE;
            trueCheckbox.selected = true;
            falseCheckbox.selected = false;
        } else if (query.get(0).equalsIgnoreCase("false")) {
            state = ConfirmedBoolean.FALSE;
            trueCheckbox.selected = false;
            falseCheckbox.selected = true;
        } else {
            state = ConfirmedBoolean.UNCONFIRMED;
            trueCheckbox.selected = false;
            falseCheckbox.selected = false;
        }
    }

    @Override
    protected void updateQuery() {
        filterScreen.removeFilter(filterScreen.getSelectedProvider());

        if (state == ConfirmedBoolean.TRUE) {
            query = "true";
        } else if (state == ConfirmedBoolean.FALSE) {
            query = "false";
        } else {
            query = "";
            return;
        }

        filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
    }
}

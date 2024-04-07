/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class StringValueWidget extends GeneralValueWidget {
    private final TextInputBoxWidget entryInput;
    private final WynntilsCheckbox allCheckbox;
    private final WynntilsCheckbox strictCheckbox;

    private boolean allQuery = false;
    private boolean ignoreUpdate = false;
    private boolean strict = false;

    public StringValueWidget(List<String> query, ItemFilterScreen filterScreen) {
        super(Component.literal("String Value Widget"), filterScreen);

        this.entryInput = new TextInputBoxWidget(
                getX() + 10,
                getY() + 60,
                150,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);

        this.strictCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 10,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.strict"),
                this.strict,
                150,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        this.strict = !this.strict;
                        ignoreUpdate = true;
                        updateQuery();
                        ignoreUpdate = false;
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.strictTooltip")));

        this.allCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 110,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.any"),
                allQuery,
                25,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        allQuery = !allQuery;
                        ignoreUpdate = true;
                        this.entryInput.setTextBoxInput(allQuery ? "*" : "");
                        updateQuery();
                        ignoreUpdate = false;
                    }
                },
                List.of(Component.translatable(
                        "screens.wynntils.itemFilter.anyTooltip",
                        filterScreen.getSelectedProvider().getDisplayName())));

        widgets.add(this.strictCheckbox);
        widgets.add(this.entryInput);
        widgets.add(this.allCheckbox);

        updateValues(query);

        updateQuery();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.stringQuery")),
                        getX() + 10,
                        getY() + 50,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    @Override
    public void updateValues(List<String> query) {
        ignoreUpdate = true;

        strict = false;
        allQuery = false;
        strictCheckbox.selected = false;
        allCheckbox.selected = false;

        StringBuilder valueBuilder = new StringBuilder();

        // Update the input widget and checkbox states based on the ItemSearchWidgets query for the
        // current provider. Check for allQuery or strict, in which case the query will only be 1 value
        for (String value : query) {
            if (value.isEmpty()) {
                entryInput.setTextBoxInput("");
                ignoreUpdate = false;
                return;
            } else if (value.equals("*")) {
                allQuery = true;
                allCheckbox.selected = true;
                entryInput.setTextBoxInput("*");
                ignoreUpdate = false;
                return;
            } else if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                // If the query contains any strict value then just set that to the query
                strict = true;
                strictCheckbox.selected = true;
                entryInput.setTextBoxInput(value.substring(1, value.length() - 1));
                ignoreUpdate = false;
                return;
            } else {
                valueBuilder.append(value);

                // Append the seperator for all sorts except the last
                if (query.indexOf(value) != query.size() - 1) {
                    valueBuilder.append(Services.ItemFilter.LIST_SEPARATOR);
                }
            }
        }

        entryInput.setTextBoxInput(valueBuilder.toString());

        ignoreUpdate = false;
    }

    @Override
    protected void updateQuery() {
        // Remove all filters for the current provider
        filterScreen.removeFilter(filterScreen.getSelectedProvider());

        if (entryInput.getTextBoxInput().isEmpty()) {
            // If no input then the checkboxes should be set to false, but only
            // if the checkboxes themselves were not what called updateQuery as the onPress for
            // checkboxes will change their state after this is finished
            if (!ignoreUpdate) {
                strictCheckbox.selected = strict;
                allCheckbox.selected = false;
            }

            return;
        }

        String query;

        // Determine the query based on the checkbox states and input widget
        if (entryInput.getTextBoxInput().equals("*")) {
            query = "*";
            allQuery = true;
        } else if (strict) {
            query = "\"" + entryInput.getTextBoxInput() + "\"";
            allQuery = false;
        } else {
            query = entryInput.getTextBoxInput();
            allQuery = false;
        }

        // Same as above, don't update these when they were what triggered updateQuery
        if (!ignoreUpdate) {
            strictCheckbox.selected = strict;
            allCheckbox.selected = allQuery;
        }

        filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
    }
}

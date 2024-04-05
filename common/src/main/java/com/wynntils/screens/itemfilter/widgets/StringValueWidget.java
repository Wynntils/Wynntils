/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

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
    private boolean strict;

    public StringValueWidget(String query, ItemFilterScreen filterScreen) {
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

        // The query will be strict if it is surrounded in "", don't mark as strict if it is only a single " however
        this.strict = query.length() > 1 && query.startsWith("\"") && query.endsWith("\"");

        String inputQuery = "";

        // Set the query without the strict marks if it is strict
        if (!this.strict) {
            inputQuery = query;

            allQuery = query.equals("*");
        } else if (query != null) {
            inputQuery = query.substring(1, query.length() - 1);
        }

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

        this.entryInput.setTextBoxInput(inputQuery);

        widgets.add(this.strictCheckbox);
        widgets.add(this.entryInput);
        widgets.add(this.allCheckbox);

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

        // Update the input widget and checkbox states based on the ItemSearchWidgets query for the
        // current provider. Check for allQuery or strict
        if (query.isEmpty()) {
            entryInput.setTextBoxInput("");
        } else if (query.get(0).equals("*")) {
            allQuery = true;
            allCheckbox.selected = true;
            entryInput.setTextBoxInput(query.get(0));
        } else if (query.get(0).length() > 1
                && query.get(0).startsWith("\"")
                && query.get(0).endsWith("\"")) {
            strict = true;
            strictCheckbox.selected = true;
            entryInput.setTextBoxInput(query.get(0).substring(1, query.get(0).length() - 1));
        } else {
            entryInput.setTextBoxInput(query.get(0));
        }

        ignoreUpdate = false;
    }

    @Override
    protected void updateQuery() {
        // Remove all filters for the current provider
        filterScreen.removeFilter(filterScreen.getSelectedProvider());

        if (entryInput.getTextBoxInput().isEmpty()) {
            query = "";

            // If no input then the checkboxes should be set to false, but only
            // if the checkboxes themselves were not what called updateQuery as the onPress for
            // checkboxes will change their state after this is finished
            if (!ignoreUpdate) {
                strictCheckbox.selected = false;
                allCheckbox.selected = false;
            }

            return;
        }

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

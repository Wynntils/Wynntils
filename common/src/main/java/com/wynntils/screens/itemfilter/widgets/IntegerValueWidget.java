/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IntegerValueWidget extends GeneralValueWidget {
    private static final Pattern SINGLE_PATTERN = Pattern.compile("(-?\\d+)%?");
    private static final Pattern RANGED_PATTERN = Pattern.compile("(-?\\d+)-(-?\\d+)%?");
    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile("(>=?)\\s*(-?\\d+)%?");
    private static final Pattern LESS_THAN_PATTERN = Pattern.compile("(<=?)\\s*(-?\\d+)%?");

    private final boolean supportsPercentage;
    private final Button greaterThanButton;
    private final Button lessThanButton;
    private final TextInputBoxWidget singleInput;
    private final TextInputBoxWidget rangedMinInput;
    private final TextInputBoxWidget rangedMaxInput;
    private final TextInputBoxWidget greaterThanInput;
    private final TextInputBoxWidget lessThanInput;
    private final WynntilsCheckbox allCheckbox;
    private WynntilsCheckbox singlePercentageCheckbox;
    private WynntilsCheckbox rangedPercentageCheckbox;
    private WynntilsCheckbox greaterThanPercentageCheckbox;
    private WynntilsCheckbox lessThanPercentageCheckbox;

    private boolean allQuery = false;
    private boolean ignoreUpdate = false;
    private boolean greaterThanEqual = false;
    private boolean lessThanEqual = false;
    private boolean singlePercentage = false;
    private boolean rangedPercentage = false;
    private boolean greaterThanPercentage = false;
    private boolean lessThanPercentage = false;

    public IntegerValueWidget(List<String> query, boolean supportsPercentage, ItemFilterScreen filterScreen) {
        super(Component.literal("Integer Value Widget"), filterScreen);

        this.supportsPercentage = supportsPercentage;

        String singleInputQuery = "";
        String minInputQuery = "";
        String maxInputQuery = "";
        String greaterThanInputQuery = "";
        String lessThanInputQuery = "";

        // Handle all of the values and determine which query type they fit into
        for (String value : query) {
            // Match any only applies to the single input
            if (value.equals("*")) {
                allQuery = true;
                singleInputQuery = value;
                this.singlePercentage = false;
                continue;
            }

            Matcher singleMatcher = SINGLE_PATTERN.matcher(value);
            Matcher rangedMatcher = RANGED_PATTERN.matcher(value);
            Matcher greaterThanMatcher = GREATER_THAN_PATTERN.matcher(value);
            Matcher lessThanMatcher = LESS_THAN_PATTERN.matcher(value);

            // Only allow one of each query type, match the first found in the list
            // Determine if the query uses the % stat instead
            // And see if the greater/less than use equal to or not
            if (singleInputQuery.isEmpty() && singleMatcher.matches()) {
                singleInputQuery = singleMatcher.group(1);
                this.singlePercentage = value.endsWith("%") && supportsPercentage;
            } else if (minInputQuery.isEmpty() && maxInputQuery.isEmpty() && rangedMatcher.matches()) {
                minInputQuery = rangedMatcher.group(1);
                maxInputQuery = rangedMatcher.group(2);
                this.rangedPercentage = value.endsWith("%") && supportsPercentage;
            } else if (greaterThanInputQuery.isEmpty() && greaterThanMatcher.matches()) {
                greaterThanInputQuery = greaterThanMatcher.group(2);
                greaterThanEqual = greaterThanMatcher.group(1).equals(">=");
                this.greaterThanPercentage = value.endsWith("%") && supportsPercentage;
            } else if (lessThanInputQuery.isEmpty() && lessThanMatcher.matches()) {
                lessThanInputQuery = lessThanMatcher.group(2);
                lessThanEqual = lessThanMatcher.group(1).equals("<=");
                this.lessThanPercentage = value.endsWith("%") && supportsPercentage;
            }
        }

        // region Inputs and options
        this.singleInput = new TextInputBoxWidget(
                getX() + 10,
                getY() + 12,
                50,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);
        this.rangedMinInput = new TextInputBoxWidget(
                getX() + 10,
                getY() + 48,
                50,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);
        this.rangedMaxInput = new TextInputBoxWidget(
                getX() + 80,
                getY() + 48,
                50,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);
        this.greaterThanInput = new TextInputBoxWidget(
                getX() + 10,
                getY() + 82,
                50,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);
        this.greaterThanButton = new Button.Builder(
                        Component.literal(greaterThanEqual ? ">=" : ">"), (button -> toggleGreaterLessThan(true)))
                .pos(getX() + 62, getY() + 82)
                .size(20, 20)
                .build();
        this.lessThanInput = new TextInputBoxWidget(
                getX() + 10,
                getY() + 116,
                50,
                20,
                (s -> {
                    if (ignoreUpdate) return;
                    updateQuery();
                }),
                filterScreen);
        this.lessThanButton = new Button.Builder(
                        Component.literal(lessThanEqual ? "<=" : "<"), (button -> toggleGreaterLessThan(false)))
                .pos(getX() + 62, getY() + 116)
                .size(20, 20)
                .build();

        allCheckbox = new WynntilsCheckbox(
                getX() + 70,
                getY() + 12,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.any"),
                allQuery,
                25,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        allQuery = !allQuery;
                        ignoreUpdate = true;
                        singleInput.setTextBoxInput(allQuery ? "*" : "");
                        updateQuery();
                        ignoreUpdate = false;
                    }
                },
                List.of(Component.translatable(
                        "screens.wynntils.itemFilter.anyTooltip",
                        filterScreen.getSelectedProvider().getDisplayName())));

        if (supportsPercentage) {
            singlePercentageCheckbox = new WynntilsCheckbox(
                    getX() + 165,
                    getY() + 12,
                    20,
                    20,
                    Component.literal("%"),
                    !allQuery && singlePercentage,
                    10,
                    (b) -> {
                        if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            singlePercentage = !singlePercentage;
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
                    !allQuery && rangedPercentage,
                    10,
                    (b) -> {
                        if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            rangedPercentage = !rangedPercentage;
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
                    !allQuery && greaterThanPercentage,
                    10,
                    (b) -> {
                        if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            greaterThanPercentage = !greaterThanPercentage;
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
                    !allQuery && greaterThanPercentage,
                    10,
                    (b) -> {
                        if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            greaterThanPercentage = !greaterThanPercentage;
                            updateQuery();
                        }
                    },
                    List.of(Component.translatable("screens.wynntils.itemFilter.percentageTooltip")));

            widgets.add(singlePercentageCheckbox);
            widgets.add(rangedPercentageCheckbox);
            widgets.add(greaterThanPercentageCheckbox);
            widgets.add(lessThanPercentageCheckbox);
        }

        widgets.add(singleInput);
        widgets.add(allCheckbox);
        widgets.add(rangedMinInput);
        widgets.add(rangedMaxInput);
        widgets.add(greaterThanInput);
        widgets.add(greaterThanButton);
        widgets.add(lessThanInput);
        widgets.add(lessThanButton);
        // endregion

        // Set the values for all of the inputs
        if (!singleInputQuery.isEmpty()) {
            singleInput.setTextBoxInput(singleInputQuery);
        }

        if (!minInputQuery.isEmpty() && !maxInputQuery.isEmpty()) {
            rangedMinInput.setTextBoxInput(minInputQuery);
            rangedMaxInput.setTextBoxInput(maxInputQuery);
        }

        if (!greaterThanInputQuery.isEmpty()) {
            greaterThanInput.setTextBoxInput(greaterThanInputQuery);
        }

        if (!lessThanInputQuery.isEmpty()) {
            lessThanInput.setTextBoxInput(lessThanInputQuery);
        }

        updateQuery();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.integerSetValue")),
                        getX() + 10,
                        getY() + 1,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.itemFilter.integerRangedValue")),
                        getX() + 10,
                        getY() + 37,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("-"),
                        getX() + 70,
                        getY() + 59,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.itemFilter.integerGreaterThanValue")),
                        getX() + 10,
                        getY() + 71,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.itemFilter.integerLessThanValue")),
                        getX() + 10,
                        getY() + 105,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    @Override
    public void updateValues(List<String> query) {
        ignoreUpdate = true;

        // Populate all of the input widgets and set the state for the checkboxes and greater/less than buttons
        // based on the query from the ItemSearchWidget.
        // ignoreUpdate is set to true so no consumers for the input widgets are called

        String singleInputQuery = "";
        String minInputQuery = "";
        String maxInputQuery = "";
        String greaterThanInputQuery = "";
        String lessThanInputQuery = "";

        greaterThanEqual = false;
        lessThanEqual = false;
        allQuery = false;
        allCheckbox.selected = false;

        for (String value : query) {
            if (value.equals("*")) {
                allQuery = true;
                singleInputQuery = value;
                this.singlePercentage = false;
                allCheckbox.selected = true;
                continue;
            }

            Matcher singleMatcher = SINGLE_PATTERN.matcher(value);
            Matcher rangedMatcher = RANGED_PATTERN.matcher(value);
            Matcher greaterThanMatcher = GREATER_THAN_PATTERN.matcher(value);
            Matcher lessThanMatcher = LESS_THAN_PATTERN.matcher(value);

            if (singleInputQuery.isEmpty() && singleMatcher.matches()) {
                singleInputQuery = singleMatcher.group(1);
                this.singlePercentage = value.endsWith("%") && supportsPercentage;
            } else if (minInputQuery.isEmpty() && maxInputQuery.isEmpty() && rangedMatcher.matches()) {
                minInputQuery = rangedMatcher.group(1);
                maxInputQuery = rangedMatcher.group(2);
                this.rangedPercentage = value.endsWith("%") && supportsPercentage;
            } else if (greaterThanInputQuery.isEmpty() && greaterThanMatcher.matches()) {
                greaterThanInputQuery = greaterThanMatcher.group(2);
                greaterThanEqual = greaterThanMatcher.group(1).equals(">=");
                this.greaterThanPercentage = value.endsWith("%") && supportsPercentage;
            } else if (lessThanInputQuery.isEmpty() && lessThanMatcher.matches()) {
                lessThanInputQuery = lessThanMatcher.group(2);
                lessThanEqual = lessThanMatcher.group(1).equals("<=");
                this.lessThanPercentage = value.endsWith("%") && supportsPercentage;
            }
        }

        singleInput.setTextBoxInput(singleInputQuery);
        rangedMinInput.setTextBoxInput(minInputQuery);
        rangedMaxInput.setTextBoxInput(maxInputQuery);
        greaterThanInput.setTextBoxInput(greaterThanInputQuery);
        lessThanInput.setTextBoxInput(lessThanInputQuery);

        if (supportsPercentage) {
            singlePercentageCheckbox.selected = !allQuery && singlePercentage;
            rangedPercentageCheckbox.selected = !allQuery && rangedPercentage;
            greaterThanPercentageCheckbox.selected = !allQuery && greaterThanPercentage;
            lessThanPercentageCheckbox.selected = !allQuery && lessThanPercentage;
        }

        greaterThanButton.setMessage(Component.literal(greaterThanEqual ? ">=" : ">"));
        lessThanButton.setMessage(Component.literal(lessThanEqual ? "<=" : "<"));

        ignoreUpdate = false;
    }

    @Override
    protected void updateQuery() {
        query = "";

        // Remove all of the filters for the current provider
        filterScreen.removeFilter(filterScreen.getSelectedProvider());

        // Add the single input if it is present and handle the allQuery and possible %
        if (!singleInput.getTextBoxInput().isEmpty()) {
            query = singleInput.getTextBoxInput();
            allQuery = query.equals("*");

            if (singlePercentage && !allQuery) {
                query += "%";
            }

            filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
        } else {
            allQuery = false;
        }

        // When the checkbox is clicked it will make ignoreUpdate true, we don't want it to be set
        // as selected here, as the checkbox onPress will toggle the selected state again after finishing
        // this method, causing it to be in the wrong state
        if (!ignoreUpdate) {
            allCheckbox.selected = allQuery;
        }

        query = "";

        // Handle the ranged query, ensuring both inputs have content
        if (!rangedMinInput.getTextBoxInput().isEmpty()
                && !rangedMaxInput.getTextBoxInput().isEmpty()) {
            query = rangedMinInput.getTextBoxInput() + "-" + rangedMaxInput.getTextBoxInput();

            if (rangedPercentage) {
                query += "%";
            }

            filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
        }

        query = "";

        // Handle the greater than query, using the button to determine equal to
        if (!greaterThanInput.getTextBoxInput().isEmpty()) {
            query = (greaterThanEqual ? ">=" : ">") + greaterThanInput.getTextBoxInput();

            if (greaterThanPercentage) {
                query += "%";
            }

            filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
        }

        query = "";

        // Same as greater than but for less than
        if (!lessThanInput.getTextBoxInput().isEmpty()) {
            query = (lessThanEqual ? "<=" : "<") + lessThanInput.getTextBoxInput();

            if (lessThanPercentage) {
                query += "%";
            }

            filterScreen.addFilter(new Pair<>(filterScreen.getSelectedProvider(), query));
        }
    }

    private void toggleGreaterLessThan(boolean greaterThan) {
        if (greaterThan) {
            greaterThanEqual = !greaterThanEqual;

            String type = greaterThanEqual ? ">=" : ">";

            greaterThanButton.setMessage(Component.literal(type));
        } else {
            lessThanEqual = !lessThanEqual;

            String type = lessThanEqual ? "<=" : "<";

            lessThanButton.setMessage(Component.literal(type));
        }

        updateQuery();
    }
}

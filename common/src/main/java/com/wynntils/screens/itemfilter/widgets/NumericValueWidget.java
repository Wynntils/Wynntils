/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class NumericValueWidget<T> extends GeneralValueWidget {
    protected final Button greaterThanButton;
    protected final Button lessThanButton;
    protected final TextInputBoxWidget singleInput;
    protected final TextInputBoxWidget rangedMinInput;
    protected final TextInputBoxWidget rangedMaxInput;
    protected final TextInputBoxWidget greaterThanInput;
    protected final TextInputBoxWidget lessThanInput;
    protected final WynntilsCheckbox allCheckbox;

    protected boolean greaterThanEqual = false;
    protected boolean lessThanEqual = false;

    protected NumericValueWidget(
            List<StatProviderAndFilterPair> filters,
            ItemStatProvider<?> itemStatProvider,
            ItemFilterScreen filterScreen) {
        super(Component.literal("Integer Value Widget"), itemStatProvider, filterScreen);

        this.singleInput = new TextInputBoxWidget(getX() + 10, getY() + 12, 50, 20, (s -> updateQuery()), filterScreen);
        this.rangedMinInput =
                new TextInputBoxWidget(getX() + 10, getY() + 48, 50, 20, (s -> updateQuery()), filterScreen);
        this.rangedMaxInput =
                new TextInputBoxWidget(getX() + 80, getY() + 48, 50, 20, (s -> updateQuery()), filterScreen);
        this.greaterThanInput =
                new TextInputBoxWidget(getX() + 10, getY() + 82, 50, 20, (s -> updateQuery()), filterScreen);
        this.greaterThanButton = new Button.Builder(Component.literal(">"), (button -> {
                    greaterThanEqual = !greaterThanEqual;
                    updateEqualityButtons();
                    updateQuery();
                }))
                .pos(getX() + 62, getY() + 82)
                .size(20, 20)
                .build();
        this.lessThanInput =
                new TextInputBoxWidget(getX() + 10, getY() + 116, 50, 20, (s -> updateQuery()), filterScreen);
        this.lessThanButton = new Button.Builder(Component.literal("<"), (button -> {
                    lessThanEqual = !lessThanEqual;
                    updateEqualityButtons();
                    updateQuery();
                }))
                .pos(getX() + 62, getY() + 116)
                .size(20, 20)
                .build();

        this.allCheckbox = new WynntilsCheckbox(
                getX() + 70,
                getY() + 12,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.any"),
                false,
                25,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        singleInput.setTextBoxInput(
                                checkbox.isActive() ? getAnyStatFilter().asString() : "");
                        updateQuery();
                    }
                },
                List.of(Component.translatable(
                        "screens.wynntils.itemFilter.anyTooltip",
                        filterScreen.getSelectedProvider().getDisplayName())));

        widgets.add(singleInput);
        widgets.add(allCheckbox);
        widgets.add(rangedMinInput);
        widgets.add(rangedMaxInput);
        widgets.add(greaterThanInput);
        widgets.add(greaterThanButton);
        widgets.add(lessThanInput);
        widgets.add(lessThanButton);
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
    public void onFiltersChanged(List<StatProviderAndFilterPair> filters) {
        // FIXME: This ValueWidget can only handle a single filter of a type at a time for the current provider
        //        (for example, only one single value filter, one ranged value filter, etc.,
        //        while the ItemFilterService supports multiple filters of the same type)

        // Reset all inputs
        greaterThanEqual = false;
        lessThanEqual = false;
        allCheckbox.selected = false;

        singleInput.resetTextBoxInput();
        rangedMinInput.resetTextBoxInput();
        rangedMaxInput.resetTextBoxInput();
        greaterThanInput.resetTextBoxInput();
        lessThanInput.resetTextBoxInput();

        // FIXME: As the fixme above states, this ValueWidget can only handle
        //        a single filter of a type at a time for the current provider.
        //        This implementation will only use the last filter of each type
        for (StatProviderAndFilterPair filterPair : filters) {
            StatFilter statFilter = filterPair.statFilter();

            if (statFilter instanceof AnyStatFilters.AbstractAnyStatFilter<?>) {
                // All checkbox
                allCheckbox.selected = true;
                singleInput.setTextBoxInput(getAnyStatFilter().asString());

                // Although we already know that this provider will always match, we still want to populate the inputs
                continue;
            }

            if (statFilter instanceof RangedStatFilters.AbstractRangedStatFilter<?> rangedStatFilter) {
                // Single value, if min and max are equal
                if (rangedStatFilter.getMin() == rangedStatFilter.getMax()) {
                    singleInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMin()));
                    continue;
                }

                // Greater than (or equals) value if min is not the minimum value, and max is the maximum value
                if (rangedStatFilter.getMin() != Integer.MIN_VALUE && rangedStatFilter.getMax() == Integer.MAX_VALUE) {
                    if (rangedStatFilter.isEqualsInString()) {
                        greaterThanEqual = true;
                        greaterThanInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMin()));

                    } else {
                        // When equality is not enabled, we need to subtract 1 from the value,
                        // as getMin() returns the lowest included value
                        greaterThanEqual = false;
                        greaterThanInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMin() - 1));
                    }
                    continue;
                }

                // Less than (or equals) value if max is not the maximum value, and min is the minimum value
                if (rangedStatFilter.getMax() != Integer.MAX_VALUE && rangedStatFilter.getMin() == Integer.MIN_VALUE) {
                    if (rangedStatFilter.isEqualsInString()) {
                        lessThanEqual = true;
                        lessThanInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMax()));

                    } else {
                        // When equality is not enabled, we need to add 1 to the value,
                        // as getMax() returns the highest included value
                        lessThanEqual = false;
                        lessThanInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMax() + 1));
                    }
                    continue;
                }

                // Ranged value, any other case
                rangedMinInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMin()));
                rangedMaxInput.setTextBoxInput(String.valueOf(rangedStatFilter.getMax()));
            } else {
                WynntilsMod.warn("Invalid filter type for NumericValueWidget: "
                        + statFilter.getClass().getSimpleName());
            }
        }

        updateEqualityButtons();
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilterPairs() {
        // FIXME: This ValueWidget can only handle a single filter of a type at a time for the current provider
        //        (for example, only one single value filter, one ranged value filter, etc.,
        //        while the ItemFilterService supports multiple filters of the same type)

        // All checkbox
        if (allCheckbox.selected()
                || singleInput.getTextBoxInput().equals(getAnyStatFilter().asString())) {
            return List.of(new StatProviderAndFilterPair(itemStatProvider, getAnyStatFilter()));
        }

        List<StatProviderAndFilterPair> filters = new ArrayList<>();

        // Single value
        if (!singleInput.getTextBoxInput().isEmpty()) {
            String value = singleInput.getTextBoxInput();
            Optional<StatFilter<T>> singleStatFilterOpt = getSingleStatFilter(value);

            singleStatFilterOpt.ifPresent(
                    singleStatFilter -> filters.add(new StatProviderAndFilterPair(itemStatProvider, singleStatFilter)));
        }

        // Ranged value
        if (!rangedMinInput.getTextBoxInput().isEmpty()
                && !rangedMaxInput.getTextBoxInput().isEmpty()) {
            String min = rangedMinInput.getTextBoxInput();
            String max = rangedMaxInput.getTextBoxInput();
            Optional<StatFilter<T>> rangedStatFilterOpt = getRangedStatFilter(min, max);

            rangedStatFilterOpt.ifPresent(
                    rangedStatFilter -> filters.add(new StatProviderAndFilterPair(itemStatProvider, rangedStatFilter)));
        }

        // Greater than value
        if (!greaterThanInput.getTextBoxInput().isEmpty()) {
            String value = greaterThanInput.getTextBoxInput();
            Optional<StatFilter<T>> greaterThanStatFilterOpt = getGreaterThanStatFilter(value, greaterThanEqual);

            greaterThanStatFilterOpt.ifPresent(greaterThanStatFilter ->
                    filters.add(new StatProviderAndFilterPair(itemStatProvider, greaterThanStatFilter)));
        }

        // Less than value
        if (!lessThanInput.getTextBoxInput().isEmpty()) {
            String value = lessThanInput.getTextBoxInput();
            Optional<StatFilter<T>> lessThanStatFilterOpt = getLessThanStatFilter(value, lessThanEqual);

            lessThanStatFilterOpt.ifPresent(lessThanStatFilter ->
                    filters.add(new StatProviderAndFilterPair(itemStatProvider, lessThanStatFilter)));
        }

        return filters;
    }

    protected abstract StatFilter<T> getAnyStatFilter();

    protected abstract Optional<StatFilter<T>> getSingleStatFilter(String value);

    protected abstract Optional<StatFilter<T>> getRangedStatFilter(String min, String max);

    protected abstract Optional<StatFilter<T>> getGreaterThanStatFilter(String value, boolean equal);

    protected abstract Optional<StatFilter<T>> getLessThanStatFilter(String value, boolean equal);

    private void updateEqualityButtons() {
        greaterThanButton.setMessage(Component.literal(greaterThanEqual ? ">=" : ">"));
        lessThanButton.setMessage(Component.literal(lessThanEqual ? "<=" : "<"));
    }
}

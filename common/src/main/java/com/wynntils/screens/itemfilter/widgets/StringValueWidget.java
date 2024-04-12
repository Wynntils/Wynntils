/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class StringValueWidget extends GeneralValueWidget {
    private final TextInputBoxWidget entryInput;
    private final WynntilsCheckbox allCheckbox;
    private final WynntilsCheckbox strictCheckbox;

    public StringValueWidget(
            List<StatProviderAndFilterPair> filters,
            ItemStatProvider<?> itemStatProvider,
            ItemFilterScreen filterScreen) {
        super(Component.literal("String Value Widget"), itemStatProvider, filterScreen);

        this.entryInput = new TextInputBoxWidget(getX() + 10, getY() + 60, 150, 20, (s -> updateQuery()), filterScreen);

        this.strictCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 10,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.strict"),
                false,
                150,
                (button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.strictTooltip")));

        this.allCheckbox = new WynntilsCheckbox(
                getX() + 10,
                getY() + 110,
                20,
                20,
                Component.translatable("screens.wynntils.itemFilter.any"),
                false,
                25,
                (checkbox, button) -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        this.entryInput.setTextBoxInput(checkbox.selected() ? "*" : "");
                        updateQuery();
                    }
                },
                List.of(Component.translatable(
                        "screens.wynntils.itemFilter.anyTooltip",
                        filterScreen.getSelectedProvider().getDisplayName())));

        widgets.add(this.strictCheckbox);
        widgets.add(this.entryInput);
        widgets.add(this.allCheckbox);
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
    public void onFiltersChanged(List<StatProviderAndFilterPair> filters) {
        // FIXME: This ValueWidget can only handle a single filter of a type at a time for the current provider
        //        (for example, only one string filter (strict or not)
        //        while the ItemFilterService supports multiple filters of the same type)

        strictCheckbox.selected = false;
        allCheckbox.selected = false;

        if (filters.isEmpty()) return;

        StatProviderAndFilterPair filterPair = filters.get(0);
        StatFilter statFilter = filterPair.statFilter();

        if (statFilter instanceof AnyStatFilters.AnyStringStatFilter) {
            allCheckbox.selected = true;
            return;
        }

        if (statFilter instanceof StringStatFilter stringStatFilter) {
            strictCheckbox.selected = stringStatFilter.isStrict();
            entryInput.setTextBoxInput(stringStatFilter.getSearchLiteral());
        } else {
            WynntilsMod.warn("String Value Widget received a non-string filter: "
                    + statFilter.getClass().getSimpleName());
        }
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilterPairs() {
        // FIXME: This ValueWidget can only handle a single filter of a type at a time for the current provider
        //        (for example, only one string filter (strict or not)
        //        while the ItemFilterService supports multiple filters of the same type)

        if (allCheckbox.selected) {
            return List.of(new StatProviderAndFilterPair(itemStatProvider, new AnyStatFilters.AnyStringStatFilter()));
        }

        String inputString =
                strictCheckbox.selected ? "\"" + entryInput.getTextBoxInput() + "\"" : entryInput.getTextBoxInput();

        Optional<StringStatFilter> stringStatFilterOpt =
                new StringStatFilter.StringStatFilterFactory().create(entryInput.getTextBoxInput());

        if (stringStatFilterOpt.isEmpty()) return List.of();

        return List.of(new StatProviderAndFilterPair(itemStatProvider, stringStatFilterOpt.get()));
    }
}

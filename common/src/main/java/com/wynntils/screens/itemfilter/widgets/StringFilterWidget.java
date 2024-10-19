/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StringFilterWidget extends GeneralFilterWidget {
    private final Button removeButton;
    private final TextInputBoxWidget entryInput;
    private final WynntilsCheckbox strictCheckbox;

    private boolean ignoreUpdate = false;

    public StringFilterWidget(
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        super(x, y, width, height, Component.literal("String Filter Widget"), parent);

        int inputWidth = width - 77;

        this.entryInput = new TextInputBoxWidget(
                getX(),
                getY(),
                inputWidth,
                getHeight(),
                (s -> {
                    if (ignoreUpdate) return;

                    parent.updateQuery();
                }),
                filterScreen);

        boolean strict = false;

        if (filterPair != null && filterPair.statFilter() instanceof StringStatFilter stringStatFilter) {
            ignoreUpdate = true;
            strict = stringStatFilter.isStrict();
            this.entryInput.setTextBoxInput(stringStatFilter.getSearchLiteral());
            ignoreUpdate = false;
        }

        this.strictCheckbox = new WynntilsCheckbox(
                getX() + inputWidth + 2,
                getY(),
                20,
                Component.translatable("screens.wynntils.itemFilter.strict"),
                strict,
                50,
                (checkbox, bl) -> {
                    if (bl) {
                        parent.updateQuery();
                    }
                },
                List.of(Component.translatable("screens.wynntils.itemFilter.strictTooltip")));

        this.removeButton = new Button.Builder(Component.literal("🗑"), (button -> parent.removeWidget(this)))
                .pos(getX() + inputWidth + 54, getY())
                .size(20, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        entryInput.render(guiGraphics, mouseX, mouseY, partialTick);
        strictCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (entryInput.isMouseOver(mouseX, mouseY)) {
            return entryInput.mouseClicked(mouseX, mouseY, button);
        } else if (strictCheckbox.isMouseOver(mouseX, mouseY)) {
            return strictCheckbox.mouseClicked(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (entryInput.isMouseOver(mouseX, mouseY)) {
            return entryInput.mouseReleased(mouseX, mouseY, button);
        } else if (strictCheckbox.isMouseOver(mouseX, mouseY)) {
            return strictCheckbox.mouseReleased(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        setY(y);

        entryInput.setY(y);
        strictCheckbox.setY(y);
        removeButton.setY(y);
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        if (entryInput.getTextBoxInput().isEmpty()) return null;

        String input =
                strictCheckbox.selected ? "\"" + entryInput.getTextBoxInput() + "\"" : entryInput.getTextBoxInput();

        Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory().create(input);

        return statFilterOpt
                .map(stringStatFilter -> new StatProviderAndFilterPair(parent.getProvider(), stringStatFilter))
                .orElse(null);
    }
}

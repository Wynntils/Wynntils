/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets.numeric;

import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.GeneralFilterWidget;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public abstract class SingleNumericFilterWidget<T> extends GeneralFilterWidget {
    private final Button removeButton;
    private final TextInputBoxWidget entryInput;

    private boolean ignoreUpdate = false;

    protected SingleNumericFilterWidget(
            int x, int y, int width, int height, ProviderFilterListWidget parent, ItemFilterScreen filterScreen) {
        super(x, y, width, height, Component.literal("Single Numeric Filter Widget"), parent);

        this.entryInput = new TextInputBoxWidget(
                getX(),
                getY(),
                width - 54,
                getHeight(),
                (s -> {
                    if (ignoreUpdate) return;

                    parent.updateQuery();
                }),
                filterScreen);

        this.removeButton = new Button.Builder(Component.literal("🗑"), (button -> parent.removeWidget(this)))
                .pos(getX() + width - 20, getY())
                .size(20, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        entryInput.render(guiGraphics, mouseX, mouseY, partialTick);
        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (entryInput.isMouseOver(mouseX, mouseY)) {
            return entryInput.mouseClicked(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (entryInput.isMouseOver(mouseX, mouseY)) {
            return entryInput.mouseReleased(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        setY(y);

        entryInput.setY(y);
        removeButton.setY(y);
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        if (entryInput.getTextBoxInput().isEmpty()) return null;

        String value = entryInput.getTextBoxInput();
        Optional<StatFilter<T>> singleStatFilterOpt = getSingleStatFilter(value);

        return singleStatFilterOpt
                .map(statFilter -> new StatProviderAndFilterPair<>(parent.getProvider(), statFilter))
                .orElse(null);
    }

    protected void setEntryInput(String input) {
        ignoreUpdate = true;
        entryInput.setTextBoxInput(input);
        ignoreUpdate = false;
    }

    protected abstract Optional<StatFilter<T>> getSingleStatFilter(String value);
}

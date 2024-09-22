/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets.numeric;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.GeneralFilterWidget;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public abstract class RangedNumericFilterWidget<T> extends GeneralFilterWidget {
    private final Button removeButton;
    private final TextInputBoxWidget minInput;
    private final TextInputBoxWidget maxInput;

    private boolean ignoreUpdate = false;

    protected RangedNumericFilterWidget(
            int x, int y, int width, int height, ProviderFilterListWidget parent, ItemFilterScreen filterScreen) {
        super(x, y, width, height, Component.literal("Ranged Numeric Filter Widget"), parent);

        this.minInput = new TextInputBoxWidget(
                getX(),
                getY(),
                50,
                getHeight(),
                (s -> {
                    if (ignoreUpdate) return;

                    parent.updateQuery();
                }),
                filterScreen);

        this.maxInput = new TextInputBoxWidget(
                getX() + 67,
                getY(),
                50,
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
        minInput.render(guiGraphics, mouseX, mouseY, partialTick);
        maxInput.render(guiGraphics, mouseX, mouseY, partialTick);
        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("-"),
                        getX() + 59,
                        getY() + 11,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (minInput.isMouseOver(mouseX, mouseY)) {
            return minInput.mouseClicked(mouseX, mouseY, button);
        } else if (maxInput.isMouseOver(mouseX, mouseY)) {
            return maxInput.mouseClicked(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (minInput.isMouseOver(mouseX, mouseY)) {
            return minInput.mouseReleased(mouseX, mouseY, button);
        } else if (maxInput.isMouseOver(mouseX, mouseY)) {
            return maxInput.mouseReleased(mouseX, mouseY, button);
        } else if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        setY(y);

        minInput.setY(y);
        maxInput.setY(y);
        removeButton.setY(y);
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        if (minInput.getTextBoxInput().isEmpty() || maxInput.getTextBoxInput().isEmpty()) return null;

        String min = minInput.getTextBoxInput();
        String max = maxInput.getTextBoxInput();
        Optional<StatFilter<T>> rangedStatFilterOpt = getRangedStatFilter(min, max);

        return rangedStatFilterOpt
                .map(statFilter -> new StatProviderAndFilterPair<>(parent.getProvider(), statFilter))
                .orElse(null);
    }

    protected void setMinInput(String input) {
        ignoreUpdate = true;
        minInput.setTextBoxInput(input);
        ignoreUpdate = false;
    }

    protected void setMaxInput(String input) {
        ignoreUpdate = true;
        maxInput.setTextBoxInput(input);
        ignoreUpdate = false;
    }

    protected abstract Optional<StatFilter<T>> getRangedStatFilter(String min, String max);
}

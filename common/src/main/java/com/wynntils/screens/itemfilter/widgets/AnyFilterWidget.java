/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class AnyFilterWidget extends GeneralFilterWidget {
    private final Button removeButton;

    protected AnyFilterWidget(int x, int y, ProviderFilterListWidget parent) {
        super(x, y, 195, 145, Component.literal("Any Filter Widget"), parent);

        this.removeButton = new Button.Builder(Component.translatable("screens.wynntils.itemFilter.removeAny"), (b -> {
                    parent.updateQuery();
                    parent.createWidgets();
                }))
                .pos(getX() + 50, getY() + getHeight() - 30)
                .size(95, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.itemFilter.anyFilterActive",
                                parent.getProvider().getDisplayName())),
                        getX(),
                        getX() + getWidth(),
                        getY(),
                        getY() + getHeight() - 30,
                        getWidth(),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        // Isn't scrollable
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        return null;
    }
}

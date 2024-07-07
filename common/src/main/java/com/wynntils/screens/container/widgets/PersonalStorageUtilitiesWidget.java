/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.container.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.inventory.PersonalStorageUtilitiesFeature;
import com.wynntils.models.containers.containers.personal.PersonalStorageContainer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PersonalStorageUtilitiesWidget extends AbstractWidget {
    private static final int BUTTON_SPACING = 18;
    private final PersonalStorageContainer container;
    private final List<QuickJumpButton> quickJumpButtons = new ArrayList<>();
    private final PersonalStorageEditNameButton editButton;
    private final PersonalStorageUtilitiesFeature feature;

    public PersonalStorageUtilitiesWidget(
            int x, int y, PersonalStorageContainer container, PersonalStorageUtilitiesFeature feature) {
        super(x, y, 100, 110, Component.literal("Personal Storage Utilities Widget"));

        this.container = container;
        this.feature = feature;

        editButton = new PersonalStorageEditNameButton(x + 86, y + 2, 14, 14);

        addJumpButtons();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.BANK_PANEL, getX(), getY());

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(Models.Bank.getPageName(Models.Bank.getCurrentPage())),
                        getX() + 4,
                        getY() + 4,
                        getWidth() - 18,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1f);

        editButton.render(guiGraphics, mouseX, mouseY, partialTick);

        quickJumpButtons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : quickJumpButtons) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (editButton.isMouseOver(mouseX, mouseY)) {
            return editButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    public void jumpToPage(int destination) {
        feature.jumpToDestination(destination);
    }

    private void addJumpButtons() {
        int renderX = getX() + 6;
        int renderY = getY() + 16;

        for (int i = 0; i < container.getFinalPage(); i++) {
            quickJumpButtons.add(new QuickJumpButton(renderX, renderY, i + 1, this));

            renderX += BUTTON_SPACING;

            if ((i + 1) % 5 == 0) {
                renderX = getX() + 6;
                renderY += BUTTON_SPACING;
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}

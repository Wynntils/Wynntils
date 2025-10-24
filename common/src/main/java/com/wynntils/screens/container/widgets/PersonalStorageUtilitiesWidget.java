/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.container.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.inventory.PersonalStorageUtilitiesFeature;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.models.containers.containers.personal.PersonalStorageContainer;
import com.wynntils.models.containers.type.QuickJumpButtonIcon;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

public class PersonalStorageUtilitiesWidget extends AbstractWidget {
    private static final int BUTTON_SPACING = 18;

    private final PersonalStorageContainer container;
    private final List<QuickJumpButton> quickJumpButtons = new ArrayList<>();
    private final PersonalStorageEditModeButton editButton;
    private final TextInputBoxWidget editInput;
    private final PersonalStorageUtilitiesFeature feature;
    private final AbstractContainerScreen<?> screen;

    private String pageName;

    public PersonalStorageUtilitiesWidget(
            int x,
            int y,
            PersonalStorageContainer container,
            PersonalStorageUtilitiesFeature feature,
            AbstractContainerScreen<?> screen) {
        super(x, y, 100, 110, Component.literal("Personal Storage Utilities Widget"));

        this.container = container;
        this.feature = feature;
        this.screen = screen;

        editButton = new PersonalStorageEditModeButton(x + 86, y + 9, 14, 14, this);

        editInput = new TextInputBoxWidget(
                getX() + 2,
                getY() + 10,
                getWidth() - 18,
                FontRenderer.getInstance().getFont().lineHeight + 2,
                null,
                (ScreenExtension) screen);
        editInput.setTextBoxInput(
                Models.Bank.getPageCustomization(Models.Bank.getCurrentPage()).getName());
        editInput.visible = false;
        screen.addRenderableWidget(editInput);

        updatePageName();
        addJumpButtons();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.BANK_PANEL, getX(), getY());

        if (!Models.Bank.isEditingMode()) {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics.pose(),
                            StyledText.fromString(pageName),
                            getX() + 4,
                            getY() + 11,
                            getWidth() - 18,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL,
                            1f);
        }

        editButton.render(guiGraphics, mouseX, mouseY, partialTick);
        editInput.render(guiGraphics, mouseX, mouseY, partialTick);

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
        toggleEditMode(false);
        feature.jumpToDestination(destination);
    }

    public void toggleEditMode(boolean on) {
        editInput.visible = on;
        editInput.setTextBoxInput(
                Models.Bank.getPageCustomization(Models.Bank.getCurrentPage()).getName());

        Models.Bank.toggleEditingMode(on);
    }

    public void updatePageName() {
        pageName =
                Models.Bank.getPageCustomization(Models.Bank.getCurrentPage()).getName();
    }

    public void updatePageIcons() {
        for (int i = 0; i < quickJumpButtons.size(); i++) {
            var button = quickJumpButtons.get(i);
            button.setIcon(Models.Bank.getPageCustomization(i + 1).getIcon());
        }
    }

    public void saveEditModeChanges() {
        feature.saveEditModeChanges();
    }

    public String getName() {
        return editInput.getTextBoxInput();
    }

    public QuickJumpButtonIcon getPageIcon(int page) {
        var button = quickJumpButtons.get(page - 1);
        return button.getIcon();
    }

    private void addJumpButtons() {
        int renderX = getX() + 6;
        int renderY = getY() + 23;

        for (int i = 0; i < container.getFinalPage(); i++) {
            quickJumpButtons.add(new QuickJumpButton(
                    renderX,
                    renderY,
                    i + 1,
                    this.feature.getLockedQuickJumpColor(),
                    this.feature.getSelectedQuickJumpColor(),
                    Models.Bank.getPageCustomization(i + 1).getIcon(),
                    this));

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

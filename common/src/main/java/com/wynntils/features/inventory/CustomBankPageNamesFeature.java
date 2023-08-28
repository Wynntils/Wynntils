/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerLabelRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.container.widgets.ContainerEditNameButton;
import com.wynntils.utils.render.FontRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class CustomBankPageNamesFeature extends Feature {
    private TextInputBoxWidget editInput;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (Models.Bank.getCurrentContainer() == null) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        screen.addRenderableWidget(screen.addRenderableWidget(new ContainerEditNameButton(
                renderX + (screen.imageWidth - screen.titleLabelX) - 10, renderY + (screen.titleLabelY) - 4, 6, 16)));
    }

    @SubscribeEvent
    public void onRenderLabels(ContainerLabelRenderEvent.ContainerLabel event) {
        if (Models.Bank.getCurrentContainer() == null) return;

        if (Models.Bank.isEditingName()) {
            event.setCanceled(true);
            addEditInput(event.getScreen());
        } else {
            if (editInput != null) {
                event.getScreen().removeWidget(editInput);
                editInput = null;
            }

            int currentPage = Models.Bank.getCurrentPage();

            if (Models.Bank.getPageName(currentPage).isPresent()) {
                String nameToRender = ChatFormatting.BLACK + "[Pg. " + currentPage + "] "
                        + Models.Bank.getPageName(currentPage).get();
                event.setContainerLabel(Component.literal(nameToRender));
            }
        }
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ENTER) return;
        if (!Models.Bank.isEditingName()) return;

        Models.Bank.saveCurrentPageName(editInput.getTextBoxInput());
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (Models.Bank.getCurrentContainer() == null) return;
        Models.Bank.toggleEditingName(false);
    }

    private void addEditInput(AbstractContainerScreen<?> screen) {
        if (editInput != null) return;

        editInput = new TextInputBoxWidget(
                screen.leftPos + screen.titleLabelX,
                screen.topPos + screen.titleLabelY,
                screen.imageWidth - 50,
                FontRenderer.getInstance().getFont().lineHeight,
                null,
                (ScreenExtension) screen);

        Models.Bank.getPageName(Models.Bank.getCurrentPage()).ifPresent(s -> editInput.setTextBoxInput(s));

        screen.addRenderableWidget(editInput);
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerLabelRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.container.widgets.ContainerEditNameButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class CustomBankPageNamesFeature extends Feature {
    private TextInputBoxWidget editInput;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (Models.Bank.getCurrentContainer() == null) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        screen.addRenderableWidget(screen.addRenderableWidget(new ContainerEditNameButton(
                renderX + (screen.imageWidth - screen.titleLabelX) - 10, renderY + (screen.titleLabelY) - 4, 6, 16)));
    }

    @SubscribeEvent
    public void onRenderLabels(ContainerLabelRenderEvent event) {
        if (Models.Bank.getCurrentContainer() == null) return;

        if (Models.Bank.isEditingName()) {
            event.setCanceled(true);
            renderEditInput(event.getScreen());
        } else {
            if (editInput != null) {
                event.getScreen().removeWidget(editInput);
                editInput = null;
            }

            if (!Models.Bank.getPageName(Models.Bank.getCurrentPage()).isEmpty()) {
                event.setCanceled(true);
                renderPageName(event.getPoseStack(), event.getScreen());
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

    private void renderEditInput(AbstractContainerScreen<?> screen) {
        if (editInput != null) return;

        editInput = new TextInputBoxWidget(
                screen.leftPos + screen.titleLabelX,
                screen.topPos + screen.titleLabelY,
                screen.imageWidth - 50,
                FontRenderer.getInstance().getFont().lineHeight,
                null,
                (ScreenExtension) screen);

        editInput.setTextBoxInput(Models.Bank.getPageName(Models.Bank.getCurrentPage()));

        screen.addRenderableWidget(editInput);
    }

    private void renderPageName(PoseStack poseStack, AbstractContainerScreen<?> screen) {
        String nameToRender =
                "[Pg. " + Models.Bank.getCurrentPage() + "] " + Models.Bank.getPageName(Models.Bank.getCurrentPage());

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(nameToRender),
                        screen.titleLabelX,
                        screen.titleLabelY,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }
}

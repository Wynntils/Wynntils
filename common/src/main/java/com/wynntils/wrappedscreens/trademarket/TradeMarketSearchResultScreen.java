/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Handlers;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class TradeMarketSearchResultScreen extends WrappedScreen {
    private TradeMarketSearchResultParent parent;

    private final ItemSearchWidget itemSearchWidget;

    protected TradeMarketSearchResultScreen(
            Screen originalScreen, AbstractContainerMenu containerMenu, int containerId) {
        super(originalScreen, containerMenu, containerId);

        if (!(originalScreen instanceof AbstractContainerScreen<?> originalContainerScreen)) {
            throw new IllegalArgumentException("originalScreen must be an AbstractContainerScreen");
        }

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (originalContainerScreen.width - originalContainerScreen.imageWidth) / 2;
        int renderY = (originalContainerScreen.height - originalContainerScreen.imageHeight) / 2 - 20;

        itemSearchWidget = new ItemSearchWidget(renderX, renderY, 175, 20, q -> reloadElements(), (TextboxScreen) this);

        parent = (TradeMarketSearchResultParent)
                Handlers.WrappedScreen.getParent(this.getClass(), TradeMarketSearchResultParent.class);
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(itemSearchWidget);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        renderables.forEach(c -> c.render(poseStack, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        return super.doMouseClicked(mouseX, mouseY, button) || originalScreen.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers)
                || originalScreen.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers) || originalScreen.charTyped(codePoint, modifiers);
    }

    private void reloadElements() {
        parent.updateItems(itemSearchWidget.getSearchQuery());
    }
}

/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;

public class TradeMarketSearchResultScreen extends WynntilsContainerScreen<ChestMenu> implements WrappedScreen {
    private static final ResourceLocation CONTAINER_BACKGROUND =
            new ResourceLocation("textures/gui/container/generic_54.png");

    private final TradeMarketSearchResultParent parent;
    private final WrappedScreenInfo wrappedScreenInfo;

    private final ItemSearchWidget itemSearchWidget;

    protected TradeMarketSearchResultScreen(WrappedScreenInfo wrappedScreenInfo, TradeMarketSearchResultParent parent) {
        super(ChestMenu.sixRows(999, McUtils.inventory()), McUtils.inventory(), Component.literal("Wrapped Screen"));

        AbstractContainerScreen<?> screen = wrappedScreenInfo.screen();
        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2 - 20;

        itemSearchWidget = new ItemSearchWidget(renderX, renderY, 175, 20, q -> reloadElements(), (TextboxScreen) this);

        this.parent = parent;
        this.wrappedScreenInfo = wrappedScreenInfo;
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
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
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        // MC code to render the background
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, this.imageWidth, this.menu.getRowCount() * 18 + 17);
        blit(poseStack, x, y + this.menu.getRowCount() * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    private void reloadElements() {
        parent.updateItems(itemSearchWidget.getSearchQuery());
    }
}

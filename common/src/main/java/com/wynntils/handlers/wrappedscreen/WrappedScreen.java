/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class WrappedScreen extends WynntilsScreen {
    protected final Screen originalScreen;
    protected final AbstractContainerMenu containerMenu;
    protected final int containerId;

    protected WrappedScreen(Screen originalScreen, AbstractContainerMenu containerMenu, int containerId) {
        super(Component.literal("Wrapped ").append(originalScreen.getTitle()));

        this.originalScreen = originalScreen;
        this.containerMenu = containerMenu;
        this.containerId = containerId;
    }

    @Override
    protected void doInit() {
        originalScreen.init();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        originalScreen.render(poseStack, mouseX, mouseY, partialTick);
    }

    public AbstractContainerMenu getContainerMenu() {
        return containerMenu;
    }

    public final int getContainerId() {
        return containerId;
    }
}

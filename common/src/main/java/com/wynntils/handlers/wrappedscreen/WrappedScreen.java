/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WrappedScreen extends WynntilsScreen {
    private final Screen originalScreen;
    private final int containerId;

    public WrappedScreen(Screen originalScreen, int containerId) {
        super(Component.literal("Wrapped ").append(originalScreen.getTitle()));

        this.originalScreen = originalScreen;
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

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        return originalScreen.mouseClicked(mouseX, mouseY, button);
    }

    public final int getContainerId() {
        return containerId;
    }
}

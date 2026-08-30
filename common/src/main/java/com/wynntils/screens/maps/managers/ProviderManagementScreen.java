/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProviderManagementScreen extends WynntilsScreen {
    private final AbstractMapScreen previousScreen;
    private int offsetX;
    private int offsetY;

    private ProviderManagementScreen(AbstractMapScreen previousScreen) {
        super(Component.literal("Provider Management Screen"));
        this.previousScreen = previousScreen;
    }

    public static Screen create(AbstractMapScreen oldMapScreen) {
        return new ProviderManagementScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }
}

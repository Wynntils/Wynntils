/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class WynntilsContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected WynntilsContainerScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    private void failure(String method, Throwable e) {
        WynntilsMod.error("Failure in " + this.getClass().getSimpleName() + "." + method + "()", e);
        McUtils.sendErrorToClient("Wynntils: Failure in " + this.getClass().getSimpleName() + " during " + method
                + ". Screen forcefully closed.");
        McUtils.setScreen(null);
    }

    @Override
    public final void init() {
        try {
            doInit();
        } catch (Throwable t) {
            failure("init", t);
        }
    }

    protected void doInit() {
        super.init();
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        try {
            doRender(guiGraphics, mouseX, mouseY, partialTick);
        } catch (Throwable t) {
            failure("render", t);
        }
    }

    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

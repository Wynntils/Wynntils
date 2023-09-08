/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.screens;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class WynntilsScreen extends Screen {
    protected WynntilsScreen(Component component) {
        super(component);
    }

    private void failure(String method, Throwable throwable) {
        McUtils.mc().setScreen(null);

        WynntilsMod.reportCrash(
                CrashType.SCREEN,
                this.getClass().getSimpleName(),
                this.getClass().getName(),
                method,
                true,
                false,
                throwable);
        McUtils.sendErrorToClient("Screen was forcefully closed.");
    }

    @Override
    protected final void init() {
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

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            return doMouseClicked(mouseX, mouseY, button);
        } catch (Throwable t) {
            failure("mouseClicked", t);
        }

        return false;
    }

    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void wrapCurrentScreenError(Runnable action, String errorDesc, String screenName) {
        try {
            action.run();
        } catch (Throwable t) {
            failure(errorDesc, t);
        }
    }
}

/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class WynntilsScreen extends Screen {
    protected WynntilsScreen(Component component) {
        super(component);
    }

    private void failure(String method, Throwable e) {
        WynntilsMod.error("Failure in " + this.getClass().getSimpleName() + "." + method + "()", e);
        McUtils.sendMessageToClient(Component.literal("Wynntils: Failure in " + method + " in "
                        + this.getClass().getSimpleName() + ". Screen forcefully closed.")
                .withStyle(ChatFormatting.RED));
        McUtils.mc().setScreen(null);
    }

    @Override
    protected final void init() {
        try {
            super.init();
            safeInit();
        } catch (Throwable t) {
            failure("init", t);
        }
    }

    protected abstract void safeInit();

    @Override
    public final void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        try {
            super.render(poseStack, mouseX, mouseY, partialTick);
            safeRender(poseStack, mouseX, mouseY, partialTick);
        } catch (Throwable t) {
            failure("init", t);
        }
    }

    public abstract void safeRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick);
}

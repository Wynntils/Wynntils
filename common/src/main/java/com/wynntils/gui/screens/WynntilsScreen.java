package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class WynntilsScreen extends Screen {
    protected WynntilsScreen(Component component) {
        super(component);
    }

    @Override
    protected final void init() {
        try {
            super.init();
            safeInit();
        } catch (Throwable e) {
            WynntilsMod.error("Crash in Screen.init() of " + this.getClass().getSimpleName(), e);
            McUtils.sendMessageToClient(new TextComponent("Crash in Screen.init() of " + this.getClass().getSimpleName()));
            McUtils.mc().setScreen(null);
        }
    }

    protected abstract void safeInit();

    @Override
    public final void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        try {
            super.render(poseStack, mouseX, mouseY, partialTick);
            safeRender(poseStack, mouseX, mouseY, partialTick);
        } catch (Throwable e) {
            WynntilsMod.error("Crash in Screen.render() of " + this.getClass().getSimpleName(), e);
            McUtils.sendMessageToClient(new TextComponent("Crash in Screen.render() of " + this.getClass().getSimpleName()));
            McUtils.mc().setScreen(null);
        }
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick, boolean marker) {
        render(poseStack, mouseX, mouseY, partialTick);
    }

    public abstract void safeRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

}

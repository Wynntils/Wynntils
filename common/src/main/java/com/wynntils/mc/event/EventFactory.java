/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.WynntilsMod;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.inventory.Slot;

/** Creates events from mixins and platform dependent hooks */
public class EventFactory {
    public static void onScreenCreated(
            Screen screen, List<AbstractWidget> buttons, Consumer<AbstractWidget> addButton) {
        System.out.println("DEBUG: onScreenCreated");
        if (screen instanceof TitleScreen titleScreen) {
            WynntilsMod.eventBus.postEvent(
                    new TitleScreenInitEvent(titleScreen, buttons, addButton));
        } else if (screen instanceof PauseScreen gameMenuScreen) {
            WynntilsMod.eventBus.postEvent(new GameMenuInitEvent(gameMenuScreen, buttons));
        }
    }

    public static void onInventoryRender(
            Screen screen,
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float partialTicks,
            Slot hoveredSlot) {
        System.out.println("DEBUG: onInventoryRender");

        WynntilsMod.eventBus.postEvent(
                new InventoryRenderEvent(
                        screen, poseStack, mouseX, mouseY, partialTicks, hoveredSlot));
    }

    public static void onTooltipRender(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        // this is done for inventory only. But why?
        // why not?
        GlStateManager._translated(0, 0, -300d);
    }
}

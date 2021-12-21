/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.listener;

import com.wynntils.mc.event.ScreenEvents;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ScreenListener {

    @SubscribeEvent
    public void onInitGuiEventPost(GuiScreenEvent.InitGuiEvent.Post e) {
        Screen screen = e.getGui();
        List<AbstractWidget> buttons = e.getWidgetList();

        ScreenEvents.onScreenCreated(screen, buttons, e::addWidget);
    }

    /*
    @SubscribeEvent
    public void onChest(GuiOverlapEvent.ChestOverlap.DrawScreen.Post e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

    @SubscribeEvent
    public void onInventory(GuiOverlapEvent.InventoryOverlap.DrawScreen e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

    @SubscribeEvent
    public void onHorse(GuiOverlapEvent.HorseOverlap.DrawScreen e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

     */
}

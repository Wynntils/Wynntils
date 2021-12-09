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

        ScreenEvents.onScreenCreated(screen, buttons);
    }
}

/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixin;

import com.wynntils.mc.event.EventFactory;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 1100)
public abstract class ScreenMixin extends AbstractContainerEventHandler
        implements TickableWidget, Widget {

    @Shadow
    protected abstract <T extends AbstractWidget> T addButton(T abstractWidget);

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void init(Minecraft client, int width, int height, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;
        List<AbstractWidget> buttons = Screens.getButtons(screen);

        EventFactory.onScreenCreated(screen, buttons, this::addButton);
    }
}

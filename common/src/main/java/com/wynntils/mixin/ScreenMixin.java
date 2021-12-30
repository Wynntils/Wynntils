/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mixin;

import com.wynntils.mc.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    protected abstract <T extends AbstractWidget> T addButton(T abstractWidget);

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void init(Minecraft client, int width, int height, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        EventFactory.onScreenCreated(screen, this::addButton);
    }
}
